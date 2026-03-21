package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.configs.R2StorageProperties;
import com.ujenzilink.ujenzilink_backend.images.dtos.R2UploadResponse;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.time.Instant;
import java.util.UUID;

public class R2StorageService {
    private final S3Client s3Client;
    private final R2StorageProperties r2Props;
    private final ImageOptimizationService optimizationService;

    public R2StorageService(S3Client s3Client,
                            R2StorageProperties r2Props,
                            ImageOptimizationService optimizationService) {
        this.s3Client = s3Client;
        this.r2Props = r2Props;
        this.optimizationService = optimizationService;
    }

    public R2UploadResponse upload(MultipartFile file, String folder) {
        try {
            byte[] optimizedBytes = optimizationService.optimize(file);

            String originalName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "image";

            String extension = extractExtension(originalName);
            String key = folder + "/" + UUID.randomUUID() + "." + extension;

            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : "application/octet-stream";

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Props.bucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) optimizedBytes.length)
                    .build();

            PutObjectResponse response = s3Client.putObject(
                    request,
                    RequestBody.fromBytes(optimizedBytes)
            );

            String fileUrl = r2Props.publicUrl() + "/" + key;

            return new R2UploadResponse(
                    originalName,
                    key,
                    fileUrl,
                    contentType,
                    optimizedBytes.length,
                    response.eTag(),
                    Instant.now()
            );

        } catch (Exception e) {
            throw new RuntimeException("R2 upload failed: " + e.getMessage(), e);
        }
    }

    public R2UploadResponse upload(MultipartFile file) {
        return upload(file, "ujenzilink/uploads");
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) return "jpg"; // fallback
        return filename.substring(lastDot + 1);
    }
}
