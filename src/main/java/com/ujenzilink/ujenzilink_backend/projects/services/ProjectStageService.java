package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryUploadResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import com.ujenzilink.ujenzilink_backend.images.services.CloudinaryService;
import com.ujenzilink.ujenzilink_backend.images.services.ImageValidationService;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectStageRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectStageResponse;
import com.ujenzilink.ujenzilink_backend.projects.models.PostPhoto;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import com.ujenzilink.ujenzilink_backend.projects.repositories.StagePhotoRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectStageRepository;
import com.ujenzilink.ujenzilink_backend.user_mgt.enums.ActivityType;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectStageService {

    @Autowired
    private ProjectStageRepository projectStageRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private StagePhotoRepository stagePhotoRepository;

    @Autowired
    private ImageValidationService imageValidationService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ActivityService activityService;

    @Transactional(rollbackFor = Exception.class)
    public ApiCustomResponse<CreateProjectStageResponse> createProjectStage(CreateProjectStageRequest request,
            List<MultipartFile> images) {
        // Get the authenticated user
        java.util.Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        // Find the project
        Project project = projectRepository.findById(request.projectId()).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        // Create new project stage
        ProjectStage stage = new ProjectStage();
        stage.setProject(project);
        stage.setDescription(request.description());
        stage.setConstructionStage(request.constructionStage());
        stage.setPostType(request.postType());
        stage.setVisibility(request.visibility() != null ? request.visibility() : "ALL_MEMBERS");
        stage.setStageCost(request.stageCost());
        stage.setTotalWorkers(request.totalWorkers());
        stage.setMaterialsUsed(request.materialsUsed());

        // Start date is day created
        stage.setStartDate(LocalDate.now());
        // End date is optional from request
        stage.setEndDate(request.endDate());

        stage.setPostedBy(user);

        ProjectStage savedStage = projectStageRepository.save(stage);

        // Handle Image Uploads
        if (images != null && !images.isEmpty()) {
            int order = 1;
            for (MultipartFile file : images) {
                if (file.isEmpty())
                    continue;

                // Validate and Extract Metadata
                ImageMetadata metadata = imageValidationService.validateAndExtractMetadata(file);

                // Upload to Cloudinary in project-images folder
                CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadImage(file,
                        "ujenzilink/project-images");

                // Create and Save Image Entity
                Image image = new Image();
                image.setUrl(uploadResponse.secureUrl());
                image.setFilename(metadata.filename());
                image.setFileType(metadata.fileType());
                image.setFileSize(metadata.fileSize());
                image.setWidth(uploadResponse.width());
                image.setHeight(uploadResponse.height());
                image.setUser(user);
                image = imageRepository.save(image);

                // Create and Save PostPhoto Entity (Link Image to Stage)
                PostPhoto postPhoto = new PostPhoto();
                postPhoto.setStage(savedStage);
                postPhoto.setImage(image);
                postPhoto.setPhotoOrder(order++);
                stagePhotoRepository.save(postPhoto);
            }
        }

        CreateProjectStageResponse response = new CreateProjectStageResponse(
                savedStage.getId(),
                "Project stage created successfully");

        // Log post creation activity
        activityService.logActivity(user, ActivityType.CREATE_PROJECT_POST, savedStage.getId());

        return new ApiCustomResponse<>(response, "Project stage created successfully", HttpStatus.CREATED.value());
    }
}
