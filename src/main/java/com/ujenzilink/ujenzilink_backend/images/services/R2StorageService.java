package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.configs.R2StorageProperties;
import com.ujenzilink.ujenzilink_backend.images.dtos.R2UploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Service
public class R2StorageService {

    private static final Logger log = LoggerFactory.getLogger(R2StorageService.class);
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

    public R2UploadResponse upload(MultipartFile file, String folder, String fileName) {
        String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "image";

        String key = folder + "/" + fileName;

        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/octet-stream";

        Path tempFile = optimizationService.optimizeToTempFile(file);
        try {
            long fileSize = Files.size(tempFile);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Props.bucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            try (InputStream inputStream = Files.newInputStream(tempFile)) {
                PutObjectResponse response = s3Client.putObject(
                        request,
                        RequestBody.fromInputStream(inputStream, fileSize)
                );

                String fileUrl = r2Props.publicUrl() + "/" + key;

                return new R2UploadResponse(
                        originalName,
                        key,
                        fileUrl,
                        contentType,
                        (int) fileSize,
                        response.eTag(),
                        Instant.now()
                );
            }

        } catch (IOException e) {
            throw new RuntimeException("R2 upload failed: " + e.getMessage(), e);
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
        }
    }

    public boolean imageExists(String key) {
        if (key == null || key.isBlank()) return false;

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(r2Props.bucketName())
                    .key(key)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteImage(String key) {
        if (key == null || key.isBlank()) return false;

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(r2Props.bucketName())
                    .key(key)
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteImageWithVerification(String key) {
        if (key == null || key.isBlank()) return true;

        boolean existsBefore = imageExists(key);
        if (!existsBefore) return false;

        boolean deleted = deleteImage(key);
        if (!deleted) return true;

        boolean existsAfter = imageExists(key);
        return existsAfter;
    }

    @Async("r2TaskExecutor")
    public void uploadFromPathAsync(Path localPath, String key, String contentType) {
        try {
            long fileSize = Files.size(localPath);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Props.bucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            try (InputStream inputStream = Files.newInputStream(localPath)) {
                s3Client.putObject(request, RequestBody.fromInputStream(inputStream, fileSize));
            }
            log.info("[R2] Async upload complete: {}", key);
        } catch (Exception e) {
            log.error("[R2] Async upload failed for key '{}': {}", key, e.getMessage(), e);
        }
    }

    @Async("r2TaskExecutor")
    public void deleteImageAsync(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(r2Props.bucketName())
                    .key(key)
                    .build());
            log.info("[R2] Async delete complete: {}", key);
        } catch (Exception e) {
            log.error("[R2] Async delete failed for key '{}': {}", key, e.getMessage(), e);
        }
    }

    /** Builds the public CDN URL for a given R2 object key. */
    public String buildPublicUrl(String key) {
        return r2Props.publicUrl() + "/" + key;
    }
}
