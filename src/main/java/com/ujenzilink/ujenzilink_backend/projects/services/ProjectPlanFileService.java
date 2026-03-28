package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.configs.R2StorageProperties;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanFileFormat;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlan;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlanFile;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectPlanFileRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectPlanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private final ProjectPlanRepository planRepository;
    private final ProjectPlanFileRepository planFileRepository;

    @Value("${folders.project-floor-plans}")
    private String floorPlansFolder;

    public ProjectPlanFileService(S3Client s3Client,
                                  R2StorageProperties r2Props,
                                  ProjectPlanRepository planRepository,
                                  ProjectPlanFileRepository planFileRepository) {
        this.s3Client = s3Client;
        this.r2Props = r2Props;
        this.planRepository = planRepository;
        this.planFileRepository = planFileRepository;
    }

    public ProjectPlanFile uploadFile(UUID planId,
                                      MultipartFile file,
                                      String displayLabel,
                                      String version,
                                      User uploadedBy) {
        ProjectPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Project plan not found: " + planId));

        validateFile(file);

        String ext = getExtension(file.getOriginalFilename());
        PlanFileFormat format = EXT_TO_FORMAT.get(ext);
        String contentType = EXT_TO_CONTENT_TYPE.getOrDefault(ext, "application/octet-stream");

        String key = floorPlansFolder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2Props.bucketName())
                .key(key)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));

        ProjectPlanFile planFile = new ProjectPlanFile();
        planFile.setPlan(plan);
        planFile.setFileName(file.getOriginalFilename());
        planFile.setFileStorageKey(key);
        planFile.setFileSize((long) bytes.length);
        planFile.setFormat(format);
        planFile.setDisplayLabel(displayLabel);
        planFile.setVersion(version);
        planFile.setUploadedBy(uploadedBy);

        return planFileRepository.save(planFile);
    }

    public List<ProjectPlanFile> uploadFiles(UUID planId,
                                              List<MultipartFile> files,
                                              User uploadedBy) {
        List<ProjectPlanFile> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            saved.add(uploadFile(planId, file, null, null, uploadedBy));
        }
        return saved;
    }

    public void deleteFile(UUID fileId) {
        ProjectPlanFile planFile = planFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Plan file not found: " + fileId));

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(r2Props.bucketName())
                .key(planFile.getFileStorageKey())
                .build());

        planFile.setDeleted(true);
        planFileRepository.save(planFile);
    }

    public List<ProjectPlanFile> getFilesForPlan(UUID planId) {
        return planFileRepository.findByPlan_IdAndIsDeletedFalseOrderByUploadedAtDesc(planId);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds the 50MB limit.");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!EXT_TO_FORMAT.containsKey(ext)) {
            throw new IllegalArgumentException(
                    "Unsupported format: ." + ext + ". Allowed: PDF, DWG, DXF, IFC, RVT, JPG, JPEG, PNG.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
