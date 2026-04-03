package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.configs.R2StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class R2StorageService {

    private static final Logger log = LoggerFactory.getLogger(R2StorageService.class);
    private final S3Client s3Client;
    private final R2StorageProperties r2Props;

    public R2StorageService(S3Client s3Client,
                            R2StorageProperties r2Props) {
        this.s3Client = s3Client;
        this.r2Props = r2Props;
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
