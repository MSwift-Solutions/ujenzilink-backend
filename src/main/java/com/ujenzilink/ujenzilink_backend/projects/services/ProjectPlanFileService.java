package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.PlanFileResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPlanBasicDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.EditProjectPlanRequest;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanFileFormat;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanPurchaseStatus;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanVisibility;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlan;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlanFile;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectPlanFileRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectMemberRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectPlanPurchaseRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectPlanRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.ujenzilink.ujenzilink_backend.images.services.R2StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

    private final ProjectRepository projectRepository;
    private final ProjectPlanRepository planRepository;
    private final ProjectPlanFileRepository planFileRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectPlanPurchaseRepository purchaseRepository;
    private final SecurityUtil securityUtil;
    private final R2StorageService r2StorageService;
    private final TransactionTemplate transactionTemplate;

    @Value("${folders.project-floor-plans}")
    private String floorPlansFolder;

    @Value("${app.upload.local-mirror-base}")
    private String localMirrorBase;

    @Value("${app.upload.public-url}")
    private String publicUrl;

    public ProjectPlanFileService(ProjectRepository projectRepository,
                                  ProjectPlanRepository planRepository,
                                  ProjectPlanFileRepository planFileRepository,
                                  ProjectMemberRepository projectMemberRepository,
                                  ProjectPlanPurchaseRepository purchaseRepository,
                                  SecurityUtil securityUtil,
                                  R2StorageService r2StorageService,
                                  TransactionTemplate transactionTemplate) {
        this.projectRepository = projectRepository;
        this.planRepository = planRepository;
        this.planFileRepository = planFileRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.purchaseRepository = purchaseRepository;
        this.securityUtil = securityUtil;
        this.r2StorageService = r2StorageService;
        this.transactionTemplate = transactionTemplate;
    }

    public ApiCustomResponse<PlanFileResponse> createPlanAndUploadFile(UUID projectId,
                                                                        MultipartFile file,
                                                                        String name,
                                                                        String description,
                                                                        BigDecimal price,
                                                                        PlanVisibility visibility,
                                                                        String displayLabel) {
        Optional<User> uploaderOpt = securityUtil.getAuthenticatedUser();
        if (uploaderOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized.", HttpStatus.UNAUTHORIZED.value());
        }
        User uploader = uploaderOpt.get();

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found.", HttpStatus.NOT_FOUND.value());
        }

        String validationError = validateFile(file);
        if (validationError != null) {
            return new ApiCustomResponse<>(null, validationError, HttpStatus.BAD_REQUEST.value());
        }

        String ext = getExtension(file.getOriginalFilename());
        PlanFileFormat format = EXT_TO_FORMAT.get(ext);
        String contentType = EXT_TO_CONTENT_TYPE.getOrDefault(ext, "application/octet-stream");
        String key = floorPlansFolder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        long fileSize = file.getSize();

        // 1. Mirror raw file to local storage (No optimization for plans)
        Path localPath = Paths.get(localMirrorBase, key);
        try {
            Files.createDirectories(localPath.getParent());
            file.transferTo(localPath);
        } catch (IOException e) {
            return new ApiCustomResponse<>(null, "Failed to cache file locally: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        // 2. Wrap DB operations in a tight transaction
        ProjectPlanFile savedFile = transactionTemplate.execute(status -> {
            ProjectPlan plan = new ProjectPlan();
            plan.setProject(project);
            plan.setName(name);
            plan.setDescription(description);
            plan.setPrice(price != null ? price : BigDecimal.ZERO);
            plan.setCurrency("KES");
            plan.setVisibility(visibility != null ? visibility : PlanVisibility.PRIVATE);
            plan.setRequiresPurchase(plan.getPrice().compareTo(BigDecimal.ZERO) > 0);
            plan.setCreatedBy(uploader);
            ProjectPlan savedPlan = planRepository.save(plan);

            ProjectPlanFile planFile = new ProjectPlanFile();
            planFile.setPlan(savedPlan);
            planFile.setFileName(file.getOriginalFilename());
            planFile.setFileStorageKey(key);
            planFile.setFileSize(fileSize);
            planFile.setFormat(format);
            planFile.setDisplayLabel(displayLabel != null ? displayLabel : name);
            planFile.setVersion("1.0");
            planFile.setUploadedBy(uploader);
            return planFileRepository.save(planFile);
        });

        // 3. Trigger Async R2 Upload
        r2StorageService.uploadFromPathAsync(localPath, key, contentType);

        return new ApiCustomResponse<>(
                PlanFileResponse.from(savedFile, publicUrl),
                "Plan and file uploaded successfully.",
                HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<Boolean> hasUserPaidForPlan(UUID planId) {
        User currentUser = securityUtil.getAuthenticatedUser().orElse(null);
        if (currentUser == null) {
            return new ApiCustomResponse<>(null, "Unauthorized.", HttpStatus.UNAUTHORIZED.value());
        }

        ProjectPlan plan = planRepository.findById(planId).orElse(null);
        if (plan == null || plan.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project plan not found.", HttpStatus.NOT_FOUND.value());
        }

        // 1. PUBLIC visibility: free for everyone
        if (plan.getVisibility() == PlanVisibility.PUBLIC) {
            return new ApiCustomResponse<>(true, "Plan is public and accessible to everyone.", HttpStatus.OK.value());
        }

        // 2. Owner/Creator: always has access regardless of visibility
        if (plan.getProject().getOwner().getId().equals(currentUser.getId()) ||
            plan.getCreatedBy().getId().equals(currentUser.getId())) {
            return new ApiCustomResponse<>(true, "Owner/Creator has unrestricted access.", HttpStatus.OK.value());
        }

        // 3. MEMBERS visibility: free for project members
        if (plan.getVisibility() == PlanVisibility.MEMBERS) {
            boolean isMember = projectMemberRepository.existsByProjectAndUserAndIsDeletedFalse(plan.getProject(), currentUser);
            if (isMember) {
                return new ApiCustomResponse<>(true, "Project member has access.", HttpStatus.OK.value());
            }
            // If not a member, we still fall through to the purchase check in case they've bought it privately
        }

        // 4. PRIVATE visibility (or non-member for MEMBERS plan): check for a purchase
        // Note: For plans explicitly marked as free (requiresPurchase=false), we grant access.
        if (!plan.isRequiresPurchase()) {
             return new ApiCustomResponse<>(true, "Plan is free and accessible.", HttpStatus.OK.value());
        }

        // Final check: confirm if payment has been COMPLETED
        boolean hasPaid = purchaseRepository.existsByPlanIdAndBuyerIdAndStatus(planId, currentUser.getId(), PlanPurchaseStatus.COMPLETED);
        
        return new ApiCustomResponse<>(hasPaid, hasPaid ? "Access granted via purchase." : "Access denied: Purchase required.", HttpStatus.OK.value());
    }

    public ApiCustomResponse<List<ProjectPlanBasicDTO>> getProjectPlans(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found.", HttpStatus.NOT_FOUND.value());
        }

        List<ProjectPlan> plans = planRepository.findByProject_IdAndIsDeletedFalseOrderByCreatedAtDesc(projectId);

        List<ProjectPlanBasicDTO> dtos = plans.stream()
                .map(plan -> new ProjectPlanBasicDTO(plan.getId(), plan.getName(), plan.getPrice()))
                .collect(Collectors.toList());

        return new ApiCustomResponse<>(dtos, "Project plans retrieved successfully.", HttpStatus.OK.value());
    }

    public ApiCustomResponse<EditProjectPlanRequest> getEditablePlanData(UUID planId) {
        ProjectPlan plan = planRepository.findById(planId).orElse(null);
        if (plan == null || plan.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project plan not found.", HttpStatus.NOT_FOUND.value());
        }

        EditProjectPlanRequest data = new EditProjectPlanRequest(
                plan.getName(),
                plan.getDescription(),
                plan.getPrice(),
                plan.getVisibility()
        );

        return new ApiCustomResponse<>(data, "Editable plan data retrieved successfully.", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<Void> editPlan(UUID planId, EditProjectPlanRequest request) {
        User currentUser = securityUtil.getAuthenticatedUser().orElse(null);
        if (currentUser == null) {
            return new ApiCustomResponse<>(null, "Unauthorized.", HttpStatus.UNAUTHORIZED.value());
        }

        ProjectPlan plan = planRepository.findById(planId).orElse(null);
        if (plan == null || plan.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project plan not found.", HttpStatus.NOT_FOUND.value());
        }

        // Only project owner or plan creator can edit
        if (!plan.getProject().getOwner().getId().equals(currentUser.getId()) &&
            !plan.getCreatedBy().getId().equals(currentUser.getId())) {
            return new ApiCustomResponse<>(null, "Forbidden: Only owner/creator can edit.", HttpStatus.FORBIDDEN.value());
        }

        if (request.name() != null) plan.setName(request.name());
        if (request.description() != null) plan.setDescription(request.description());
        if (request.price() != null) {
            plan.setPrice(request.price());
            plan.setRequiresPurchase(plan.getPrice().compareTo(BigDecimal.ZERO) > 0);
        }
        if (request.visibility() != null) plan.setVisibility(request.visibility());

        planRepository.save(plan);

        return new ApiCustomResponse<>(null, "Plan updated successfully.", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<Void> deletePlan(UUID planId) {
        User currentUser = securityUtil.getAuthenticatedUser().orElse(null);
        if (currentUser == null) {
            return new ApiCustomResponse<>(null, "Unauthorized.", HttpStatus.UNAUTHORIZED.value());
        }

        ProjectPlan plan = planRepository.findById(planId).orElse(null);
        if (plan == null || plan.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project plan not found.", HttpStatus.NOT_FOUND.value());
        }

        // Only project owner or plan creator can delete
        if (!plan.getProject().getOwner().getId().equals(currentUser.getId()) &&
            !plan.getCreatedBy().getId().equals(currentUser.getId())) {
            return new ApiCustomResponse<>(null, "Forbidden: Only owner/creator can delete.", HttpStatus.FORBIDDEN.value());
        }

        // Soft delete the plan
        plan.setDeleted(true);
        planRepository.save(plan);

        // Soft delete all associated files
        List<ProjectPlanFile> files = planFileRepository.findByPlan_IdAndIsDeletedFalse(planId);
        for (ProjectPlanFile file : files) {
            file.setDeleted(true);
            planFileRepository.save(file);
        }

        return new ApiCustomResponse<>(null, "Project plan deleted successfully.", HttpStatus.OK.value());
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
