package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.configs.R2StorageProperties;
import com.ujenzilink.ujenzilink_backend.projects.dtos.PlanFileResponse;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanFileFormat;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanVisibility;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlan;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlanFile;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectPlanFileRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectPlanRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectPlanFileService {

    private static final long MAX_FILE_SIZE = 52_428_800L; // 50MB

    private static final Map<String, PlanFileFormat> EXT_TO_FORMAT = Map.of(
            "pdf",  PlanFileFormat.PDF,
            "dwg",  PlanFileFormat.DWG,
            "dxf",  PlanFileFormat.DXF,
            "ifc",  PlanFileFormat.IFC,
            "rvt",  PlanFileFormat.RVT,
            "jpg",  PlanFileFormat.JPG,
            "jpeg", PlanFileFormat.JPEG,
            "png",  PlanFileFormat.PNG
    );

    private static final Map<String, String> EXT_TO_CONTENT_TYPE = Map.of(
            "pdf",  "application/pdf",
            "dwg",  "application/acad",
            "dxf",  "application/dxf",
            "ifc",  "application/x-step",
            "rvt",  "application/octet-stream",
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "png",  "image/png"
    );

    private final S3Client s3Client;
    private final R2StorageProperties r2Props;
    private final ProjectRepository projectRepository;
    private final ProjectPlanRepository planRepository;
    private final ProjectPlanFileRepository planFileRepository;
    private final SecurityUtil securityUtil;

    @Value("${folders.project-floor-plans}")
    private String floorPlansFolder;

    public ProjectPlanFileService(S3Client s3Client,
                                  R2StorageProperties r2Props,
                                  ProjectRepository projectRepository,
                                  ProjectPlanRepository planRepository,
                                  ProjectPlanFileRepository planFileRepository,
                                  SecurityUtil securityUtil) {
        this.s3Client = s3Client;
        this.r2Props = r2Props;
        this.projectRepository = projectRepository;
        this.planRepository = planRepository;
        this.planFileRepository = planFileRepository;
        this.securityUtil = securityUtil;
    }

    public ApiCustomResponse<PlanFileResponse> createPlanAndUploadFile(UUID projectId,
                                                                       MultipartFile file,
                                                                       String name,
                                                                       String description,
                                                                       BigDecimal price,
                                                                       PlanVisibility visibility,
                                                                       String displayLabel) {
        User uploader = securityUtil.getAuthenticatedUser().orElse(null);
        if (uploader == null) {
            return new ApiCustomResponse<>(null, "Unauthorized.", HttpStatus.UNAUTHORIZED.value());
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found.", HttpStatus.NOT_FOUND.value());
        }

        // Handle version internally
        String internalVersion = "1.0";

        String validationError = validateFile(file);
        if (validationError != null) {
            return new ApiCustomResponse<>(null, validationError, HttpStatus.BAD_REQUEST.value());
        }

        String ext = getExtension(file.getOriginalFilename());
        PlanFileFormat format = EXT_TO_FORMAT.get(ext);
        String contentType = EXT_TO_CONTENT_TYPE.getOrDefault(ext, "application/octet-stream");
        String key = floorPlansFolder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            return new ApiCustomResponse<>(null, "Failed to read file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(r2Props.bucketName())
                            .key(key)
                            .contentType(contentType)
                            .contentLength((long) bytes.length)
                            .build(),
                    RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            return new ApiCustomResponse<>(null, "R2 upload failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        ProjectPlan plan = new ProjectPlan();
        plan.setProject(project);
        plan.setName(name);
        plan.setDescription(description);
        plan.setPrice(price != null ? price : BigDecimal.ZERO);
        plan.setCurrency("KES");
        plan.setVisibility(visibility != null ? visibility : PlanVisibility.PRIVATE);
        // Free plans don't strictly require a purchase record, or a 0 amount is logged.
        plan.setRequiresPurchase(plan.getPrice().compareTo(BigDecimal.ZERO) > 0);
        plan.setCreatedBy(uploader);

        ProjectPlan savedPlan = planRepository.save(plan);

        ProjectPlanFile planFile = new ProjectPlanFile();
        planFile.setPlan(savedPlan);
        planFile.setFileName(file.getOriginalFilename());
        planFile.setFileStorageKey(key);
        planFile.setFileSize((long) bytes.length);
        planFile.setFormat(format);
        planFile.setDisplayLabel(displayLabel != null ? displayLabel : name);
        planFile.setVersion(internalVersion);
        planFile.setUploadedBy(uploader);

        ProjectPlanFile savedFile = planFileRepository.save(planFile);

        return new ApiCustomResponse<>(
                PlanFileResponse.from(savedFile, r2Props.publicUrl()),
                "Plan and file uploaded successfully.",
                HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<Void> deletePlanFile(UUID fileId) {
        User currentUser = securityUtil.getAuthenticatedUser().orElse(null);
        if (currentUser == null) {
            return new ApiCustomResponse<>(null, "Unauthorized.", HttpStatus.UNAUTHORIZED.value());
        }

        ProjectPlanFile planFile = planFileRepository.findById(fileId).orElse(null);
        if (planFile == null || planFile.isDeleted()) {
            return new ApiCustomResponse<>(null, "Plan file not found.", HttpStatus.NOT_FOUND.value());
        }

        planFile.setDeleted(true);
        planFileRepository.save(planFile);

        return new ApiCustomResponse<>(null, "File deleted successfully.", HttpStatus.OK.value());
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return "File must not be empty.";
        if (file.getSize() > MAX_FILE_SIZE) return "File exceeds the 50MB limit.";
        String ext = getExtension(file.getOriginalFilename());
        if (!EXT_TO_FORMAT.containsKey(ext)) {
            return "Unsupported format: ." + ext + ". Allowed: PDF, DWG, DXF, IFC, RVT, JPG, JPEG, PNG.";
        }
        return null;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
