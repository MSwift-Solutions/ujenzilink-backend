package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.configs.R2StorageProperties;
import com.ujenzilink.ujenzilink_backend.images.dtos.R2UploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.time.Instant;

@Service
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

    public R2UploadResponse upload(MultipartFile file, String folder, String fileName) {
        try {
            byte[] optimizedBytes = optimizationService.optimize(file);

            String originalName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "image";

            String key = folder + "/" + fileName;

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
}
