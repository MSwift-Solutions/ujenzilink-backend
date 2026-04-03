package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import com.ujenzilink.ujenzilink_backend.images.services.R2StorageService;
import com.ujenzilink.ujenzilink_backend.images.services.ImageValidationService;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectStageRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectStageResponse;
import com.ujenzilink.ujenzilink_backend.projects.models.StagePhoto;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import com.ujenzilink.ujenzilink_backend.projects.repositories.StagePhotoRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectStageRepository;
import com.ujenzilink.ujenzilink_backend.user_mgt.enums.ActivityType;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.ActivityService;
import com.ujenzilink.ujenzilink_backend.notifications.services.NotificationService;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectMemberRepository;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectMember;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.ujenzilink.ujenzilink_backend.images.services.ImageOptimizationService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectStageService {

    private final ProjectStageRepository projectStageRepository;
    private final ProjectRepository projectRepository;
    private final ImageRepository imageRepository;
    private final StagePhotoRepository stagePhotoRepository;
    private final ImageValidationService imageValidationService;
    private final ImageOptimizationService imageOptimizationService;
    private final R2StorageService r2StorageService;
    private final SecurityUtil securityUtil;
    private final ActivityService activityService;
    private final NotificationService notificationService;
    private final ProjectMemberRepository projectMemberRepository;
    private final TransactionTemplate transactionTemplate;

    @Value("${folders.project-stage-images}")
    private String projectStageImagesFolder;

    @Value("${app.upload.local-mirror-base}")
    private String localMirrorBase;

    public ProjectStageService(
            ProjectStageRepository projectStageRepository,
            ProjectRepository projectRepository,
            ImageRepository imageRepository,
            StagePhotoRepository stagePhotoRepository,
            ImageValidationService imageValidationService,
            ImageOptimizationService imageOptimizationService,
            R2StorageService r2StorageService,
            SecurityUtil securityUtil,
            ActivityService activityService,
            NotificationService notificationService,
            ProjectMemberRepository projectMemberRepository,
            TransactionTemplate transactionTemplate) {
        this.projectStageRepository = projectStageRepository;
        this.projectRepository = projectRepository;
        this.imageRepository = imageRepository;
        this.stagePhotoRepository = stagePhotoRepository;
        this.imageValidationService = imageValidationService;
        this.imageOptimizationService = imageOptimizationService;
        this.r2StorageService = r2StorageService;
        this.securityUtil = securityUtil;
        this.activityService = activityService;
        this.notificationService = notificationService;
        this.projectMemberRepository = projectMemberRepository;
        this.transactionTemplate = transactionTemplate;
    }

    private record PreparedImage(Path localPath, String key, String contentType, ImageMetadata metadata) {}

    public ApiCustomResponse<CreateProjectStageResponse> createProjectStage(CreateProjectStageRequest request,
            List<MultipartFile> images) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        Project project = projectRepository.findById(request.projectId()).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        List<PreparedImage> preparedImages = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;

                ImageMetadata metadata = imageValidationService.validateAndExtractMetadata(file);
                String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                String ext = originalName.substring(originalName.lastIndexOf(".") + 1);
                
                // key: projects/stages/{projectId}/{stageId}/{uuid}.ext
                // But we don't have stageId yet, so we use a stable subfolder for the project
                String folder = projectStageImagesFolder + "/" + project.getId() + "/batch-" + UUID.randomUUID();
                String fileName = "img-" + UUID.randomUUID() + "." + ext;
                String key = folder + "/" + fileName;

                String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
                Path localPath = Paths.get(localMirrorBase, key);

                imageOptimizationService.optimizeToPath(file, localPath);
                preparedImages.add(new PreparedImage(localPath, key, contentType, metadata));
            }
        }

        ProjectStage savedStage = transactionTemplate.execute(status -> {
            ProjectStage stage = new ProjectStage();
            stage.setProject(project);
            stage.setDescription(request.description());
            stage.setConstructionStage(request.constructionStage());
            stage.setPostType(request.postType());
            stage.setVisibility(request.visibility() != null ? request.visibility() : "ALL_MEMBERS");
            stage.setStageCost(request.stageCost());
            stage.setTotalWorkers(request.totalWorkers());
            stage.setMaterialsUsed(request.materialsUsed());
            stage.setStartDate(LocalDate.now());
            stage.setEndDate(request.endDate());
            stage.setPostedBy(user);

            ProjectStage sStage = projectStageRepository.save(stage);

            int order = 1;
            for (PreparedImage prep : preparedImages) {
                Image image = new Image();
                image.setUrl(prep.key());
                image.setFilename(prep.metadata().filename());
                image.setFileType(prep.metadata().fileType());
                image.setFileSize(prep.metadata().fileSize());
                image.setUser(user);
                Image sImage = imageRepository.save(image);

                StagePhoto stagePhoto = new StagePhoto();
                stagePhoto.setStage(sStage);
                stagePhoto.setImage(sImage);
                stagePhoto.setPhotoOrder(order++);
                stagePhotoRepository.save(stagePhoto);
            }
            return sStage;
        });

        // Async R2 uploads
        for (PreparedImage prep : preparedImages) {
            r2StorageService.uploadFromPathAsync(prep.localPath(), prep.key(), prep.contentType());
        }

        CreateProjectStageResponse response = new CreateProjectStageResponse(
                savedStage.getId(),
                "Project stage created successfully");

        activityService.logActivity(user, ActivityType.CREATE_PROJECT_POST, savedStage.getId());

        List<ProjectMember> members = projectMemberRepository.findByProjectAndIsDeletedFalse(project);
        for (ProjectMember member : members) {
            if (member.getUser().getId().equals(user.getId())) continue;

            notificationService.createNotification(
                    member.getUser(), user, NotificationType.PROJECT_STAGE_UPDATE,
                    "Project Stage Update",
                    "New stage update for '" + project.getTitle() + "': " + savedStage.getConstructionStage().name(),
                    NotificationPriority.MEDIUM, false, null, null);
        }

        return new ApiCustomResponse<>(response, "Project stage created successfully", HttpStatus.CREATED.value());
    }
}
