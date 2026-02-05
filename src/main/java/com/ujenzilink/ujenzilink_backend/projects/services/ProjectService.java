package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectListResponse;
import com.ujenzilink.ujenzilink_backend.projects.enums.ConstructionStage;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectStatus;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectVisibility;
import com.ujenzilink.ujenzilink_backend.projects.enums.BudgetVisibility;
import com.ujenzilink.ujenzilink_backend.projects.enums.MemberRole;
import com.ujenzilink.ujenzilink_backend.projects.enums.PostType;

import com.ujenzilink.ujenzilink_backend.projects.models.StagePhoto;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectMember;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectCommentRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.StagePhotoRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectMemberRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectDetailsResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectStatsDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ConstructionStageDTO;
import com.ujenzilink.ujenzilink_backend.projects.utils.ProjectUtils;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPostResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.DropdownResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectDropdownsResponse;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectType;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectImageResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.UpdateProjectVisibilityRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectVisibilityResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.EditProjectRequest;
import com.ujenzilink.ujenzilink_backend.user_mgt.enums.ActivityType;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.ActivityService;

@Service
public class ProjectService {

        @Autowired
        private ProjectRepository projectRepository;

        @Autowired
        private ProjectStageRepository projectStageRepository;

        @Autowired
        private ProjectMemberRepository projectMemberRepository;

        @Autowired
        private StagePhotoRepository stagePhotoRepository;

        @Autowired
        private ProjectCommentRepository projectCommentRepository;

        @Autowired
        private com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectFollowRepository projectFollowRepository;

        @Autowired
        private com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectLikeRepository projectLikeRepository;

        @Autowired
        private SecurityUtil securityUtil;

        @Autowired
        private ActivityService activityService;

        public ApiCustomResponse<ProjectDetailsResponse> getProjectDetails(UUID projectId) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                // Increment impressions when project is fetched
                ProjectUtils.incrementImpressions(project);
                projectRepository.save(project);

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
                                .sum() + 1; // Include project creator

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

                List<ProjectPostResponse> postResponses = stages.stream()
                                .map(this::mapToProjectPostResponse)
                                .collect(Collectors.toList());

                return new ApiCustomResponse<>(postResponses, "Project posts retrieved successfully",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<List<ProjectPostResponse>> getLatestProjectPosts(UUID projectId) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                List<ProjectStage> stages = projectStageRepository
                                .findTop3ByProject_IdOrderByCreatedAtDesc(project.getId());

                List<ProjectPostResponse> postResponses = stages.stream()
                                .map(this::mapToProjectPostResponse)
                                .collect(Collectors.toList());

                return new ApiCustomResponse<>(postResponses, "Latest project posts retrieved successfully",
                                HttpStatus.OK.value());
        }

        private ProjectPostResponse mapToProjectPostResponse(ProjectStage stage) {
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
                        postedBy = new CreatorInfoDTO(poster.getId(), posterName, username, profilePictureUrl);
                }

                // Get images
                List<String> images = new ArrayList<>();
                for (StagePhoto photo : stage.getPhotos()) {
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
        }

        public ApiCustomResponse<Long> getProjectPostsCount(UUID projectId) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                long count = projectStageRepository.countByProject_Id(projectId);

                return new ApiCustomResponse<>(count, "Project posts count retrieved successfully",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<Long> getProjectImagesCount(UUID projectId) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                long count = stagePhotoRepository.countByStage_Project_Id(projectId);

                return new ApiCustomResponse<>(count, "Project images count retrieved successfully",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<List<ProjectImageResponse>> getProjectImages(UUID projectId) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                List<StagePhoto> photos = stagePhotoRepository.findByStage_Project_IdOrderByUploadedAtDesc(projectId);

                List<ProjectImageResponse> imageResponses = photos.stream()
                                .map(photo -> new ProjectImageResponse(
                                                photo.getImage() != null ? photo.getImage().getUrl() : null,
                                                photo.getUploadedAt(),
                                                photo.getStage() != null
                                                                ? ProjectUtils.formatEnumName(photo.getStage()
                                                                                .getConstructionStage().name())
                                                                : "General"))
                                .collect(Collectors.toList());

                return new ApiCustomResponse<>(imageResponses, "Project images retrieved successfully",
                                HttpStatus.OK.value());
        }

        @Transactional(rollbackFor = Exception.class)
        public ApiCustomResponse<CreateProjectResponse> createProject(CreateProjectRequest request) {
                // Get the authenticated user from SecurityUtil
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();
                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found. Please log in again.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User user = userOpt.get();

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

                // Automatically add creator as an OWNER member of the project
                ProjectMember member = new ProjectMember();
                member.setProject(savedProject);
                member.setUser(user);
                member.setAddedBy(user);
                member.setRole(MemberRole.OWNER);
                member.setCanViewProject(true);
                member.setCanManageStages(true);
                member.setCanCreatePosts(true);
                member.setCanUploadDocuments(true);
                member.setCanManageMembers(true);
                projectMemberRepository.save(member);

                CreateProjectResponse response = new CreateProjectResponse(
                                savedProject.getId(),
                                savedProject.getTitle(),
                                "Project created successfully");

                // Log project creation activity
                activityService.logActivity(user, ActivityType.CREATE_PROJECT, savedProject.getId());

                return new ApiCustomResponse<>(
                                response,
                                "Project created successfully.",
                                HttpStatus.CREATED.value());
        }

        public ApiCustomResponse<com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse> getAllProjects(
                        String cursor, Integer size) {
                // Validate and set defaults
                if (size == null || size < 1) {
                        size = 20;
                }
                if (size > 100) {
                        size = 100;
                }

                // Decode cursor to get timestamp
                Instant cursorTime = null;
                if (cursor != null && !cursor.isEmpty()) {
                        try {
                                String decodedJson = new String(java.util.Base64.getDecoder().decode(cursor));
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                java.util.Map<String, Object> cursorData = mapper.readValue(decodedJson,
                                                new TypeReference<java.util.Map<String, Object>>() {
                                                });
                                String timestamp = (String) cursorData.get("timestamp");
                                cursorTime = Instant.parse(timestamp);
                        } catch (Exception e) {
                                return new ApiCustomResponse<>(null, "Invalid cursor format",
                                                HttpStatus.BAD_REQUEST.value());
                        }
                }

                // Query database - fetch size + 1 to check if more exist
                org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                                size + 1, sort);

                List<Project> projects;
                if (cursorTime != null) {
                        projects = projectRepository.findByVisibilityAndIsDeletedFalseAndCreatedAtBefore(
                                        ProjectVisibility.PUBLIC, cursorTime, pageable);
                } else {
                        projects = projectRepository.findByVisibilityAndIsDeletedFalse(
                                        ProjectVisibility.PUBLIC, pageable);
                }

                // Add randomization (pseudo-random but consistent per project ID)
                projects.sort((p1, p2) -> {
                        int hash1 = Math.abs(p1.getId().hashCode() % 1000);
                        int hash2 = Math.abs(p2.getId().hashCode() % 1000);
                        int hashCompare = Integer.compare(hash1, hash2);
                        return hashCompare != 0 ? hashCompare : p2.getCreatedAt().compareTo(p1.getCreatedAt());
                });

                // Determine if there are more projects
                boolean hasMore = projects.size() > size;
                if (hasMore) {
                        projects = projects.subList(0, size);
                }

                // Build response list
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
                        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(creator.getId(), creatorName, username,
                                        profilePictureUrl);

                        // Get member count
                        int memberCount = projectMemberRepository.findByProjectAndIsDeletedFalse(project).size();

                        // Get current stage
                        String currentStage = null;
                        List<ProjectStage> stages = projectStageRepository
                                        .findByProjectOrderByCreatedAtAsc(project);

                        int commentsCount = 0;
                        int likesCount = 0;
                        List<String> projectImages = new ArrayList<>();

                        // Aggregate data from all stages (collect top 3 latest images)
                        List<ProjectStage> reversedStages = new ArrayList<>(stages);
                        Collections.reverse(reversedStages);

                        for (ProjectStage stage : reversedStages) {
                                if (projectImages.size() >= 3)
                                        break;
                                List<StagePhoto> stagePhotos = stagePhotoRepository.findByStageOrderByPhotoOrder(stage);
                                for (StagePhoto photo : stagePhotos) {
                                        if (photo.getImage() != null && !photo.getImage().getIsDeleted()) {
                                                projectImages.add(photo.getImage().getUrl());
                                                if (projectImages.size() >= 3)
                                                        break;
                                        }
                                }
                        }
                        if (!stages.isEmpty()) {
                                // Find first IN_PROGRESS stage or default to last stage
                                ProjectStage activeStage = stages.stream()
                                                .filter(s -> s.getStatus().name().contains("IN_PROGRESS")
                                                                || s.getStatus().name().equals("PLANNING_PERMITS"))
                                                .findFirst()
                                                .orElse(stages.get(stages.size() - 1));
                                String stageEnumName = activeStage.getStatus().name();
                                currentStage = ProjectUtils.formatEnumName(stageEnumName);
                        } else {
                                // Default to initial stage if no stages exist
                                currentStage = ProjectUtils.formatEnumName(ConstructionStage.PLANNING_PERMITS.name());
                        }

                        // Get follow count
                        int followCount = (int) projectFollowRepository.countByProjectAndIsActiveTrue(project);

                        // Get project likes count
                        likesCount = (int) projectLikeRepository.countByProject(project);

                        // Get project comments count
                        commentsCount = (int) projectCommentRepository.countByProjectAndIsDeletedFalse(project);

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
                                        followCount,
                                        currentStage);

                        projectResponses.add(response);
                }

                // Generate next cursor using last project's timestamp
                String nextCursor = null;
                if (hasMore && !projects.isEmpty()) {
                        try {
                                Project lastProject = projects.get(projects.size() - 1);
                                String cursorJson = String.format("{\"timestamp\":\"%s\"}",
                                                lastProject.getCreatedAt().toString());
                                nextCursor = java.util.Base64.getEncoder()
                                                .encodeToString(cursorJson.getBytes());
                        } catch (Exception e) {
                                // If cursor generation fails, just don't include it
                        }
                }

                com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse pageResponse = new com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse(
                                projectResponses, nextCursor, hasMore);

                return new ApiCustomResponse<>(
                                pageResponse,
                                "Projects retrieved successfully",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse> getMyProjects(
                        String cursor, Integer size) {
                // Get the authenticated user
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();
                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found. Please log in again.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User currentUser = userOpt.get();

                // Validate and set defaults
                if (size == null || size < 1) {
                        size = 20;
                }
                if (size > 100) {
                        size = 100;
                }

                // Decode cursor to get timestamp
                Instant cursorTime = null;
                if (cursor != null && !cursor.isEmpty()) {
                        try {
                                String decodedJson = new String(java.util.Base64.getDecoder().decode(cursor));
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                java.util.Map<String, Object> cursorData = mapper.readValue(decodedJson,
                                                new TypeReference<java.util.Map<String, Object>>() {
                                                });
                                String timestamp = (String) cursorData.get("timestamp");
                                cursorTime = Instant.parse(timestamp);
                        } catch (Exception e) {
                                return new ApiCustomResponse<>(null, "Invalid cursor format",
                                                HttpStatus.BAD_REQUEST.value());
                        }
                }

                // Query database - fetch size + 1 to check if more exist
                org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                                size + 1, sort);

                List<Project> projects;
                if (cursorTime != null) {
                        projects = projectRepository.findByCreatedByAndIsDeletedFalseAndCreatedAtBefore(
                                        currentUser, cursorTime, pageable);
                } else {
                        projects = projectRepository.findByCreatedByAndIsDeletedFalse(
                                        currentUser, pageable);
                }

                // Add randomization (pseudo-random but consistent per project ID)
                projects.sort((p1, p2) -> {
                        int hash1 = Math.abs(p1.getId().hashCode() % 1000);
                        int hash2 = Math.abs(p2.getId().hashCode() % 1000);
                        int hashCompare = Integer.compare(hash1, hash2);
                        return hashCompare != 0 ? hashCompare : p2.getCreatedAt().compareTo(p1.getCreatedAt());
                });

                // Determine if there are more projects
                boolean hasMore = projects.size() > size;
                if (hasMore) {
                        projects = projects.subList(0, size);
                }

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
                        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(creator.getId(), creatorName, username,
                                        profilePictureUrl);

                        // Get member count
                        int memberCount = projectMemberRepository.findByProjectAndIsDeletedFalse(project).size();

                        // Get current stage
                        String currentStage = null;
                        List<ProjectStage> stages = projectStageRepository
                                        .findByProjectOrderByCreatedAtAsc(project);

                        int commentsCount = 0;
                        int likesCount = 0;
                        List<String> projectImages = new ArrayList<>();

                        // Aggregate data from all stages (collect top 3 latest images)
                        List<ProjectStage> reversedStages = new ArrayList<>(stages);
                        Collections.reverse(reversedStages);

                        for (ProjectStage stage : reversedStages) {
                                if (projectImages.size() >= 3)
                                        break;
                                List<StagePhoto> stagePhotos = stagePhotoRepository.findByStageOrderByPhotoOrder(stage);
                                for (StagePhoto photo : stagePhotos) {
                                        if (photo.getImage() != null && !photo.getImage().getIsDeleted()) {
                                                projectImages.add(photo.getImage().getUrl());
                                                if (projectImages.size() >= 3)
                                                        break;
                                        }
                                }
                        }
                        if (!stages.isEmpty()) {
                                // Find first IN_PROGRESS stage or default to last stage
                                ProjectStage activeStage = stages.stream()
                                                .filter(s -> s.getStatus().name().contains("IN_PROGRESS")
                                                                || s.getStatus().name().equals("PLANNING_PERMITS"))
                                                .findFirst()
                                                .orElse(stages.get(stages.size() - 1));
                                String stageEnumName = activeStage.getStatus().name();
                                currentStage = ProjectUtils.formatEnumName(stageEnumName);
                        } else {
                                // Default to initial stage if no stages exist
                                currentStage = ProjectUtils.formatEnumName(ConstructionStage.PLANNING_PERMITS.name());
                        }

                        // Get follow count
                        int followCount = (int) projectFollowRepository.countByProjectAndIsActiveTrue(project);

                        // Get project likes count
                        likesCount = (int) projectLikeRepository.countByProject(project);

                        // Get project comments count
                        commentsCount = (int) projectCommentRepository.countByProjectAndIsDeletedFalse(project);

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
                                        followCount,
                                        currentStage);

                        projectResponses.add(response);
                }

                // Generate next cursor using last project's timestamp
                String nextCursor = null;
                if (hasMore && !projects.isEmpty()) {
                        try {
                                Project lastProject = projects.get(projects.size() - 1);
                                String cursorJson = String.format("{\"timestamp\":\"%s\"}",
                                                lastProject.getCreatedAt().toString());
                                nextCursor = java.util.Base64.getEncoder()
                                                .encodeToString(cursorJson.getBytes());
                        } catch (Exception e) {
                                // If cursor generation fails, just don't include it
                        }
                }

                com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse pageResponse = new com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse(
                                projectResponses, nextCursor, hasMore);

                return new ApiCustomResponse<>(
                                pageResponse,
                                "My projects retrieved successfully",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse> getFollowedProjects(
                        String cursor, Integer size) {
                // Get the authenticated user
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();
                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found. Please log in again.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User currentUser = userOpt.get();

                // Validate and set defaults
                if (size == null || size < 1) {
                        size = 20;
                }
                if (size > 100) {
                        size = 100;
                }

                // Decode cursor to get timestamp
                Instant cursorTime = null;
                if (cursor != null && !cursor.isEmpty()) {
                        try {
                                String decodedJson = new String(java.util.Base64.getDecoder().decode(cursor));
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                java.util.Map<String, Object> cursorData = mapper.readValue(decodedJson,
                                                new TypeReference<java.util.Map<String, Object>>() {
                                                });
                                String timestamp = (String) cursorData.get("timestamp");
                                cursorTime = Instant.parse(timestamp);
                        } catch (Exception e) {
                                return new ApiCustomResponse<>(null, "Invalid cursor format",
                                                HttpStatus.BAD_REQUEST.value());
                        }
                }

                // Get all active follows for this user
                List<com.ujenzilink.ujenzilink_backend.projects.models.ProjectFollow> follows = projectFollowRepository
                                .findByUserAndIsActiveTrue(currentUser);

                // Extract projects from follows and apply cursor filter
                Instant finalCursorTime = cursorTime;
                List<Project> projects = follows.stream()
                                .map(com.ujenzilink.ujenzilink_backend.projects.models.ProjectFollow::getProject)
                                .filter(project -> !project.isDeleted())
                                .filter(project -> finalCursorTime == null
                                                || project.getCreatedAt().isBefore(finalCursorTime))
                                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt())) // DESC by createdAt
                                .limit(size + 1) // Fetch size + 1 to check if more exist
                                .collect(Collectors.toList());

                // Add randomization (pseudo-random but consistent per project ID)
                projects.sort((p1, p2) -> {
                        int hash1 = Math.abs(p1.getId().hashCode() % 1000);
                        int hash2 = Math.abs(p2.getId().hashCode() % 1000);
                        int hashCompare = Integer.compare(hash1, hash2);
                        return hashCompare != 0 ? hashCompare : p2.getCreatedAt().compareTo(p1.getCreatedAt());
                });

                // Determine if there are more projects
                boolean hasMore = projects.size() > size;
                if (hasMore) {
                        projects = projects.subList(0, size);
                }

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
                        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(creator.getId(), creatorName, username,
                                        profilePictureUrl);

                        // Get member count
                        int memberCount = projectMemberRepository.findByProjectAndIsDeletedFalse(project).size();

                        // Get current stage
                        String currentStage = null;
                        List<ProjectStage> stages = projectStageRepository
                                        .findByProjectOrderByCreatedAtAsc(project);

                        int commentsCount = 0;
                        int likesCount = 0;
                        List<String> projectImages = new ArrayList<>();

                        // Aggregate data from all stages (collect top 3 latest images)
                        List<ProjectStage> reversedStages = new ArrayList<>(stages);
                        Collections.reverse(reversedStages);

                        for (ProjectStage stage : reversedStages) {
                                if (projectImages.size() >= 3)
                                        break;
                                List<StagePhoto> stagePhotos = stagePhotoRepository.findByStageOrderByPhotoOrder(stage);
                                for (StagePhoto photo : stagePhotos) {
                                        if (photo.getImage() != null && !photo.getImage().getIsDeleted()) {
                                                projectImages.add(photo.getImage().getUrl());
                                                if (projectImages.size() >= 3)
                                                        break;
                                        }
                                }
                        }
                        if (!stages.isEmpty()) {
                                // Find first IN_PROGRESS stage or default to last stage
                                ProjectStage activeStage = stages.stream()
                                                .filter(s -> s.getStatus().name().contains("IN_PROGRESS")
                                                                || s.getStatus().name().equals("PLANNING_PERMITS"))
                                                .findFirst()
                                                .orElse(stages.get(stages.size() - 1));
                                String stageEnumName = activeStage.getStatus().name();
                                currentStage = ProjectUtils.formatEnumName(stageEnumName);
                        } else {
                                // Default to initial stage if no stages exist
                                currentStage = ProjectUtils.formatEnumName(ConstructionStage.PLANNING_PERMITS.name());
                        }

                        // Get follow count
                        int followCount = (int) projectFollowRepository.countByProjectAndIsActiveTrue(project);

                        // Get project likes count
                        likesCount = (int) projectLikeRepository.countByProject(project);

                        // Get project comments count
                        commentsCount = (int) projectCommentRepository.countByProjectAndIsDeletedFalse(project);

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
                                        followCount,
                                        currentStage);

                        projectResponses.add(response);
                }

                // Generate next cursor using last project's timestamp
                String nextCursor = null;
                if (hasMore && !projects.isEmpty()) {
                        try {
                                Project lastProject = projects.get(projects.size() - 1);
                                String cursorJson = String.format("{\"timestamp\":\"%s\"}",
                                                lastProject.getCreatedAt().toString());
                                nextCursor = java.util.Base64.getEncoder()
                                                .encodeToString(cursorJson.getBytes());
                        } catch (Exception e) {
                                // If cursor generation fails, just don't include it
                        }
                }

                com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse pageResponse = new com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse(
                                projectResponses, nextCursor, hasMore);

                return new ApiCustomResponse<>(
                                pageResponse,
                                "Followed projects retrieved successfully",
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

                List<DropdownResponse> constructionStages = Arrays.stream(ConstructionStage.values())
                                .map(stage -> new DropdownResponse(stage.name(),
                                                ProjectUtils.formatEnumName(stage.name())))
                                .toList();

                List<DropdownResponse> postTypes = Arrays.stream(PostType.values())
                                .map(postType -> new DropdownResponse(postType.name(),
                                                ProjectUtils.formatEnumName(postType.name())))
                                .toList();

                ProjectDropdownsResponse response = new ProjectDropdownsResponse(
                                types,
                                statuses,
                                visibilities,
                                budgetVisibilities,
                                constructionStages,
                                postTypes);

                return new ApiCustomResponse<>(response, "Project dropdowns retrieved successfully",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<Void> deleteProject(UUID projectId) {
                // Get the authenticated user
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();
                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found. Please log in again.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User currentUser = userOpt.get();

                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                // Check permission: only owner/creator can delete
                if (!project.getOwner().getId().equals(currentUser.getId())) {
                        return new ApiCustomResponse<>(null, "You do not have permission to delete this project.",
                                        HttpStatus.FORBIDDEN.value());
                }

                project.setDeleted(true);
                projectRepository.save(project);

                // Log project deletion activity
                activityService.logActivity(currentUser, ActivityType.DELETE_PROJECT, projectId);

                return new ApiCustomResponse<>(null, "Project deleted successfully", HttpStatus.OK.value());
        }

        @Transactional(rollbackFor = Exception.class)
        public ApiCustomResponse<Void> updateProjectVisibility(UUID projectId, UpdateProjectVisibilityRequest request) {
                // Get the authenticated user
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();
                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found. Please log in again.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User currentUser = userOpt.get();

                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                // Check permission: only owner/creator can update visibility
                if (!project.getOwner().getId().equals(currentUser.getId())) {
                        return new ApiCustomResponse<>(null, "You do not have permission to update this project.",
                                        HttpStatus.FORBIDDEN.value());
                }

                // Enforce budget visibility rule: if project is private, budget can't be public
                if (request.visibility() == ProjectVisibility.PRIVATE
                                && project.getBudgetVisibility() == BudgetVisibility.PUBLIC) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "Budget cannot remain public if project visibility is set to private. Please change budget visibility first or set project to public.",
                                        HttpStatus.BAD_REQUEST.value());
                }

                project.setVisibility(request.visibility());
                projectRepository.saveAndFlush(project);

                return new ApiCustomResponse<>(null, "Project visibility updated successfully", HttpStatus.OK.value());
        }

        @Transactional(readOnly = true)
        public ApiCustomResponse<ProjectVisibilityResponse> getProjectVisibility(UUID projectId) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                ProjectVisibility visibility = project.getVisibility();
                ProjectVisibilityResponse response = new ProjectVisibilityResponse(
                                visibility,
                                ProjectUtils.formatEnumName(visibility.name()));

                return new ApiCustomResponse<>(response, "Project visibility retrieved successfully",
                                HttpStatus.OK.value());
        }

        @Transactional(rollbackFor = Exception.class)
        public ApiCustomResponse<Void> editProject(UUID projectId, EditProjectRequest request) {
                // Get the authenticated user
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();
                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found. Please log in again.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User currentUser = userOpt.get();

                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                // Check permission: only owner/creator can edit
                if (!project.getOwner().getId().equals(currentUser.getId())) {
                        return new ApiCustomResponse<>(null, "You do not have permission to edit this project.",
                                        HttpStatus.FORBIDDEN.value());
                }

                // Update fields if provided
                if (request.title() != null && !request.title().isBlank()) {
                        project.setTitle(request.title());
                }
                if (request.description() != null) {
                        project.setDescription(request.description());
                }
                if (request.projectType() != null) {
                        project.setProjectType(request.projectType());
                }
                if (request.projectStatus() != null) {
                        project.setProjectStatus(request.projectStatus());
                }
                if (request.location() != null) {
                        project.setLocation(request.location());
                }

                // Validate and update dates
                LocalDate startDate = request.startDate() != null ? request.startDate() : project.getStartDate();
                LocalDate endDate = request.expectedEndDate() != null ? request.expectedEndDate()
                                : project.getExpectedEndDate();

                if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "Expected end date cannot be before start date.",
                                        HttpStatus.BAD_REQUEST.value());
                }

                if (request.startDate() != null) {
                        project.setStartDate(request.startDate());
                }
                if (request.expectedEndDate() != null) {
                        project.setExpectedEndDate(request.expectedEndDate());
                }

                // Update budget fields
                if (request.estimatedBudget() != null) {
                        if (request.estimatedBudget().compareTo(BigDecimal.ZERO) < 0) {
                                return new ApiCustomResponse<>(null, "Estimated budget cannot be negative.",
                                                HttpStatus.BAD_REQUEST.value());
                        }
                        project.setEstimatedBudget(request.estimatedBudget());
                }
                if (request.contractValue() != null) {
                        if (request.contractValue().compareTo(BigDecimal.ZERO) < 0) {
                                return new ApiCustomResponse<>(null, "Contract value cannot be negative.",
                                                HttpStatus.BAD_REQUEST.value());
                        }
                        project.setContractValue(request.contractValue());
                }
                if (request.currency() != null) {
                        project.setCurrency(request.currency());
                }

                projectRepository.save(project);

                // Log project update activity
                activityService.logActivity(currentUser, ActivityType.UPDATE_PROJECT, projectId);

                return new ApiCustomResponse<>(null, "Project updated successfully", HttpStatus.OK.value());
        }

        @Transactional(readOnly = true)
        public ApiCustomResponse<EditProjectRequest> getEditableProjectData(UUID projectId) {
                // Get the authenticated user
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();
                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found. Please log in again.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User currentUser = userOpt.get();

                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.isDeleted()) {
                        return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
                }

                // Check permission: only owner/creator can access editable data
                if (!project.getOwner().getId().equals(currentUser.getId())) {
                        return new ApiCustomResponse<>(null,
                                        "You do not have permission to access this project's editable data.",
                                        HttpStatus.FORBIDDEN.value());
                }

                EditProjectRequest response = new EditProjectRequest(
                                project.getTitle(),
                                project.getDescription(),
                                project.getProjectType(),
                                project.getProjectStatus(),
                                project.getLocation(),
                                project.getStartDate(),
                                project.getExpectedEndDate(),
                                project.getEstimatedBudget(),
                                project.getContractValue(),
                                project.getCurrency());

                return new ApiCustomResponse<>(response, "Project editable data retrieved successfully",
                                HttpStatus.OK.value());
        }
}
