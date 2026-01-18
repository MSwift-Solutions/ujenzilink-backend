package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import java.math.BigDecimal;
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
import com.ujenzilink.ujenzilink_backend.projects.models.Post;
import com.ujenzilink.ujenzilink_backend.projects.models.PostPhoto;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import com.ujenzilink.ujenzilink_backend.projects.repositories.PostCommentRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.PostPhotoRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.PostRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectMemberRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        private PostRepository postRepository;

        @Autowired
        private PostPhotoRepository postPhotoRepository;

        @Autowired
        private PostCommentRepository postCommentRepository;

        @Transactional(rollbackFor = Exception.class)
        public ApiCustomResponse<CreateProjectResponse> createProject(CreateProjectRequest request) {
                // Get the authenticated user from security context
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
                defaultStage.setStageName("Planning & Permits");
                defaultStage.setDescription("Design, blueprints, and legal approvals");
                defaultStage.setStageOrder(1);
                defaultStage.setStatus(ConstructionStage.PLANNING_PERMITS);
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
                                .collect(Collectors.toList());

                List<ProjectListResponse> projectResponses = new ArrayList<>();

                for (Project project : projects) {
                        // Get creator information
                        User creator = project.getCreatedBy();
                        String creatorName = creator.getFirstName() + " " + creator.getLastName();
                        String profilePictureUrl = creator.getProfilePicture() != null
                                        ? creator.getProfilePicture().getUrl()
                                        : null;
                        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(creatorName, profilePictureUrl);

                        // Get member count
                        int memberCount = projectMemberRepository.findByProject(project).size();

                        // Get project images from all posts
                        List<String> projectImages = new ArrayList<>();
                        List<Post> posts = postRepository.findByProjectAndIsDeletedFalseOrderByCreatedAtDesc(project);
                        for (Post post : posts) {
                                List<PostPhoto> postPhotos = postPhotoRepository.findByPostOrderByPhotoOrder(post);
                                for (PostPhoto postPhoto : postPhotos) {
                                        if (postPhoto.getImage() != null && !postPhoto.getImage().getIsDeleted()) {
                                                projectImages.add(postPhoto.getImage().getUrl());
                                        }
                                }
                        }

                        // Get comments count (aggregate from all posts)
                        int commentsCount = posts.stream()
                                        .mapToInt(post -> (int) postCommentRepository
                                                        .countByPostAndIsDeletedFalse(post))
                                        .sum();

                        // Get likes count (currently 0 - placeholder for when likes are implemented)
                        int likesCount = 0;

                        // Get current stage
                        String currentStage = null;
                        List<ProjectStage> stages = projectStageRepository
                                        .findByProjectOrderByStageOrder(project);
                        if (!stages.isEmpty()) {
                                // Find first IN_PROGRESS stage or default to last stage
                                ProjectStage activeStage = stages.stream()
                                                .filter(s -> s.getStatus().name().contains("IN_PROGRESS")
                                                                || s.getStatus().name().equals("PLANNING_PERMITS"))
                                                .findFirst()
                                                .orElse(stages.get(stages.size() - 1));
                                currentStage = activeStage.getStageName();
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
}
