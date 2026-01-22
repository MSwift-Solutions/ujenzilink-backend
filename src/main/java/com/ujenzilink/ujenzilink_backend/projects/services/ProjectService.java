package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectListResponse;
import com.ujenzilink.ujenzilink_backend.projects.enums.ConstructionStage;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectStatus;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectVisibility;
import com.ujenzilink.ujenzilink_backend.projects.enums.BudgetVisibility;

import com.ujenzilink.ujenzilink_backend.projects.models.PostPhoto;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import com.ujenzilink.ujenzilink_backend.projects.repositories.PostCommentRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.PostPhotoRepository;
// import com.ujenzilink.ujenzilink_backend.projects.repositories.PostRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectMemberRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectDetailsResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectStatsDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ConstructionStageDTO;
import java.util.UUID;
import java.util.Arrays;
import com.ujenzilink.ujenzilink_backend.projects.utils.ProjectUtils;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPostResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.DropdownResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectDropdownsResponse;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectType;

@Service
public class ProjectService {

        @Autowired
        private ProjectRepository projectRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProjectStageRepository projectStageRepository;

        @Autowired
        private ProjectMemberRepository projectMemberRepository;

        @Autowired
        private PostPhotoRepository postPhotoRepository;

        @Autowired
        private PostCommentRepository postCommentRepository;

        public ApiCustomResponse<ProjectDetailsResponse> getProjectDetails(UUID projectId) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                List<ProjectStage> stages = projectStageRepository.findByProjectOrderByCreatedAtAsc(project);

                // Determine current stage (the one with the highest ordinal)
                ConstructionStage currentStage = ConstructionStage.PLANNING_PERMITS;
                int maxOrdinal = -1;
                for (ProjectStage stage : stages) {
                        if (stage.getConstructionStage() != null
                                        && stage.getConstructionStage().ordinal() > maxOrdinal) {
                                maxOrdinal = stage.getConstructionStage().ordinal();
                                currentStage = stage.getConstructionStage();
                        }
                }

                // Stats calculation
                int totalWorkers = stages.stream()
                                .filter(s -> s.getTotalWorkers() != null)
                                .mapToInt(ProjectStage::getTotalWorkers)
                                .sum();

                int postsCount = stages.size();
                int impressions = project.getImpressions() != null ? project.getImpressions() : 0;

                // Progress calculation
                ConstructionStage[] allStages = ConstructionStage.values();
                int currentStageIndex = currentStage.ordinal();
                int totalStagesCount = allStages.length;
                int progress = ProjectUtils.calculateRandomizedProgress(currentStageIndex, totalStagesCount,
                                projectId.getMostSignificantBits());

                ProjectStatsDTO stats = new ProjectStatsDTO(totalWorkers, progress, impressions, postsCount);

                // Construction stages list
                ConstructionStage finalCurrentStage = currentStage;
                List<ConstructionStageDTO> stageDTOs = Arrays.stream(allStages).map(s -> {
                        String name = ProjectUtils.formatEnumName(s.name());

                        boolean completed = s.ordinal() < finalCurrentStage.ordinal();
                        boolean active = s.ordinal() == finalCurrentStage.ordinal();
                        boolean upcoming = s.ordinal() > finalCurrentStage.ordinal();

                        return new ConstructionStageDTO(name, completed, active, upcoming);
                }).collect(Collectors.toList());

                ProjectDetailsResponse response = new ProjectDetailsResponse(
                                project.getDescription(),
                                stats,
                                stageDTOs);

                return new ApiCustomResponse<>(response, "Project details retrieved successfully",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<List<ProjectPostResponse>> getProjectPosts(UUID projectId) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                List<ProjectStage> stages = projectStageRepository.findByProject_IdOrderByCreatedAtAsc(project.getId());

                List<ProjectPostResponse> postResponses = stages.stream().map(stage -> {
                        // Get poster info
                        User poster = stage.getPostedBy();
                        CreatorInfoDTO postedBy = null;
                        if (poster != null) {
                                String posterName = poster.getFullName();
                                String profilePictureUrl = (poster.getProfilePicture() != null)
                                                ? poster.getProfilePicture().getUrl()
                                                : "https://ui-avatars.com/api/?name=" + posterName.replace(" ", "+")
                                                                + "&background=random";
                                String username = (poster.getUserHandle() != null && !poster.getUserHandle().isEmpty())
                                                ? poster.getUserHandle()
                                                : poster.getEmail();
                                postedBy = new CreatorInfoDTO(posterName, username, profilePictureUrl);
                        }

                        // Get images
                        List<String> images = new ArrayList<>();
                        for (PostPhoto photo : stage.getPhotos()) {
                                if (photo.getImage() != null && !photo.getImage().getIsDeleted()) {
                                        images.add(photo.getImage().getUrl());
                                }
                        }

                        // Format stage name
                        String stageName = ProjectUtils.formatEnumName(stage.getConstructionStage().name());

                        return new ProjectPostResponse(
                                        stage.getId(),
                                        stage.getDescription(),
                                        stageName,
                                        stage.getPostType() != null ? stage.getPostType().name() : null,
                                        stage.getVisibility(),
                                        stage.getStageCost(),
                                        stage.getTotalWorkers(),
                                        stage.getMaterialsUsed(),
                                        stage.getStartDate(),
                                        stage.getEndDate(),
                                        stage.getCreatedAt(),
                                        postedBy,
                                        images,
                                        stage.getCommentsCount() != null ? stage.getCommentsCount() : 0,
                                        stage.getLikesCount() != null ? stage.getLikesCount() : 0);
                }).collect(Collectors.toList());

                return new ApiCustomResponse<>(postResponses, "Project posts retrieved successfully",
                                HttpStatus.OK.value());
        }

        @Transactional(rollbackFor = Exception.class)
        public ApiCustomResponse<CreateProjectResponse> createProject(CreateProjectRequest request) {
                // Get the authenticated user from security context
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assert authentication != null;
                String userEmail = authentication.getName();

                User user = userRepository.findFirstByEmail(userEmail);
                if (user == null) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found. Please log in again.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                // Validate dates if both are provided
                if (request.startDate() != null && request.expectedEndDate() != null) {
                        if (request.expectedEndDate().isBefore(request.startDate())) {
                                return new ApiCustomResponse<>(
                                                null,
                                                "Expected end date cannot be before start date.",
                                                HttpStatus.BAD_REQUEST.value());
                        }
                }

                // Validate budget values
                if (request.estimatedBudget() != null && request.estimatedBudget().compareTo(BigDecimal.ZERO) < 0) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "Estimated budget cannot be negative.",
                                        HttpStatus.BAD_REQUEST.value());
                }

                if (request.contractValue() != null && request.contractValue().compareTo(BigDecimal.ZERO) < 0) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "Contract value cannot be negative.",
                                        HttpStatus.BAD_REQUEST.value());
                }

                // Ensure if project is set to private, budget can't be public
                ProjectVisibility visibility = request.visibility() != null ? request.visibility()
                                : ProjectVisibility.PRIVATE;
                BudgetVisibility budgetVisibility = request.budgetVisibility();

                if (visibility == ProjectVisibility.PRIVATE && budgetVisibility == BudgetVisibility.PUBLIC) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "Budget cannot be public if project is private.",
                                        HttpStatus.BAD_REQUEST.value());
                }

                // Create new project
                Project project = new Project();
                project.setTitle(request.title());
                project.setDescription(request.description());
                project.setProjectType(request.projectType());

                // Set defaults for optional fields with defaults
                project.setProjectStatus(request.projectStatus() != null
                                ? request.projectStatus()
                                : ProjectStatus.PLANNING);

                project.setVisibility(request.visibility() != null
                                ? request.visibility()
                                : ProjectVisibility.PRIVATE);

                // Set optional location field
                project.setLocation(request.location());

                // Set optional date fields
                project.setStartDate(request.startDate());
                project.setExpectedEndDate(request.expectedEndDate());

                // Set optional budget fields
                project.setEstimatedBudget(request.estimatedBudget());
                project.setContractValue(request.contractValue());
                project.setCurrency(request.currency());
                project.setBudgetVisibility(request.budgetVisibility());

                // Set owner and creator (both are the authenticated user)
                project.setOwner(user);
                project.setCreatedBy(user);

                // Save project
                Project savedProject = projectRepository.save(project);

                // Create default PLANNING_PERMITS stage
                ProjectStage defaultStage = new ProjectStage();
                defaultStage.setProject(savedProject);
                // defaultStage.setStageName(null); // Removed field
                defaultStage.setDescription("Design, blueprints, and legal approvals");
                // defaultStage.setStageOrder(1); // Removed field
                defaultStage.setStatus(ConstructionStage.PLANNING_PERMITS);
                defaultStage.setStartDate(request.startDate() != null ? request.startDate() : LocalDate.now());
                defaultStage.setPostedBy(user);
                projectStageRepository.save(defaultStage);

                CreateProjectResponse response = new CreateProjectResponse(
                                savedProject.getId(),
                                savedProject.getTitle(),
                                "Project created successfully");

                return new ApiCustomResponse<>(
                                response,
                                "Project created successfully.",
                                HttpStatus.CREATED.value());
        }

        public ApiCustomResponse<List<ProjectListResponse>> getAllProjects() {
                // Fetch all non-deleted projects
                List<Project> projects = projectRepository.findAll().stream()
                                .filter(p -> !p.isDeleted())
                                .toList();

                List<ProjectListResponse> projectResponses = new ArrayList<>();

                for (Project project : projects) {
                        // Get creator information
                        User creator = project.getCreatedBy();
                        String creatorName = creator.getFullName();
                        String profilePictureUrl = (creator.getProfilePicture() != null)
                                        ? creator.getProfilePicture().getUrl()
                                        : "https://ui-avatars.com/api/?name=" + creatorName.replace(" ", "+")
                                                        + "&background=random";
                        String username = (creator.getUserHandle() != null && !creator.getUserHandle().isEmpty())
                                        ? creator.getUserHandle()
                                        : creator.getEmail();
                        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(creatorName, username, profilePictureUrl);

                        // Get member count
                        int memberCount = projectMemberRepository.findByProject(project).size();

                        // Get current stage
                        String currentStage = null;
                        List<ProjectStage> stages = projectStageRepository
                                        .findByProjectOrderByCreatedAtAsc(project);

                        int commentsCount = 0;
                        int likesCount = 0;
                        List<String> projectImages = new ArrayList<>();

                        // Aggregate data from all stages
                        for (ProjectStage stage : stages) {
                                // Add comments count from each stage
                                commentsCount += stage.getCommentsCount() != null ? stage.getCommentsCount() : 0;

                                // Add likes count from each stage
                                likesCount += stage.getLikesCount() != null ? stage.getLikesCount() : 0;

                                // Fetch photos directly linked to the stage
                                List<PostPhoto> stagePhotos = postPhotoRepository.findByStageOrderByPhotoOrder(stage);
                                for (PostPhoto photo : stagePhotos) {
                                        if (photo.getImage() != null && !photo.getImage().getIsDeleted()) {
                                                projectImages.add(photo.getImage().getUrl());
                                        }
                                }
                        }
                        if (!stages.isEmpty()) {
                                // Find first IN_PROGRESS stage or default to last stage
                                ProjectStage activeStage = stages.stream()
                                                .filter(s -> s.getStatus().name().contains("IN_PROGRESS")
                                                                || s.getStatus().name().equals("PLANNING_PERMITS"))
                                                .findFirst()
                                                .orElse(stages.getLast());
                                String stageEnumName = activeStage.getStatus().name();
                                currentStage = ProjectUtils.formatEnumName(stageEnumName);
                        }

                        // Build response
                        ProjectListResponse response = new ProjectListResponse(
                                        project.getId(),
                                        project.getTitle(),
                                        project.getProjectType(),
                                        project.getProjectStatus(),
                                        project.getLocation(),
                                        project.getCreatedAt(),
                                        creatorInfo,
                                        memberCount,
                                        projectImages,
                                        project.getEstimatedBudget(),
                                        project.getCurrency(),
                                        likesCount,
                                        commentsCount,
                                        currentStage);

                        projectResponses.add(response);
                }

                return new ApiCustomResponse<>(
                                projectResponses,
                                "Projects retrieved successfully",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<ProjectDropdownsResponse> getProjectDropdowns() {
                List<DropdownResponse> types = Arrays.stream(ProjectType.values())
                                .map(type -> new DropdownResponse(type.name(),
                                                ProjectUtils.formatEnumName(type.name())))
                                .toList();

                List<DropdownResponse> statuses = Arrays.stream(ProjectStatus.values())
                                .map(status -> new DropdownResponse(status.name(),
                                                ProjectUtils.formatEnumName(status.name())))
                                .toList();

                List<DropdownResponse> visibilities = Arrays.stream(ProjectVisibility.values())
                                .map(visibility -> new DropdownResponse(visibility.name(),
                                                ProjectUtils.formatEnumName(visibility.name())))
                                .toList();

                List<DropdownResponse> budgetVisibilities = Arrays.stream(BudgetVisibility.values())
                                .map(budget -> new DropdownResponse(budget.name(),
                                                ProjectUtils.formatEnumName(budget.name())))
                                .toList();

                ProjectDropdownsResponse response = new ProjectDropdownsResponse(
                                types,
                                statuses,
                                visibilities,
                                budgetVisibilities);

                return new ApiCustomResponse<>(response, "Project dropdowns retrieved successfully",
                                HttpStatus.OK.value());
        }
}
