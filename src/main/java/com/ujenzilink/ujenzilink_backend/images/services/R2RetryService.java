package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.R2StorageProperties;
import com.ujenzilink.ujenzilink_backend.images.dtos.AsyncOperationLogDTO;
import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationStatus;
import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationType;
import com.ujenzilink.ujenzilink_backend.images.models.AsyncOperationLog;
import com.ujenzilink.ujenzilink_backend.images.repositories.AsyncOperationLogRepository;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Service
public class R2RetryService {

    private static final Logger log = LoggerFactory.getLogger(R2RetryService.class);

    private final AsyncOperationLogRepository logRepository;
    private final AsyncOperationLogService logService;
    private final S3Client s3Client;
    private final R2StorageProperties r2Props;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public R2RetryService(
            AsyncOperationLogRepository logRepository,
            AsyncOperationLogService logService,
            S3Client s3Client,
            R2StorageProperties r2Props,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.logRepository = logRepository;
        this.logService = logService;
        this.s3Client = s3Client;
        this.r2Props = r2Props;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public AsyncOperationLogDTO retryOperation(UUID logId) {
        AsyncOperationLog entry = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No async operation log found with id: " + logId));

        if (entry.getStatus() == AsyncOperationStatus.RESOLVED) {
            throw new IllegalStateException(
                    "Operation " + logId + " is already resolved — no retry needed.");
        }

        try {
            if (entry.getOperationType() == AsyncOperationType.UPLOAD) {
                performUpload(entry);
            } else {
                performDelete(entry);
            }

            entry.setStatus(AsyncOperationStatus.RESOLVED);
            entry.setResolvedAt(Instant.now());
            logRepository.save(entry);

            log.info("[R2Retry] {} RESOLVED — key='{}' logId='{}' attempts={}",
                    entry.getOperationType(), entry.getStorageKey(), logId, entry.getRetryCount() + 1);

            sendRecoveryNotification(entry);

        } catch (Exception ex) {
            entry.setStatus(AsyncOperationStatus.RETRIED);
            entry.setRetriedAt(Instant.now());
            entry.setRetryCount(entry.getRetryCount() + 1);
            entry.setErrorMessage(ex.getMessage());
            logRepository.save(entry);

            log.error("[R2Retry] {} retry #{} FAILED — key='{}' logId='{}': {}",
                    entry.getOperationType(), entry.getRetryCount(),
                    entry.getStorageKey(), logId, ex.getMessage(), ex);

            throw new RuntimeException(
                    "Retry attempt #" + entry.getRetryCount() + " failed: " + ex.getMessage(), ex);
        }

        return logService.toDTO(entry);
    }

    private void performUpload(AsyncOperationLog entry) throws Exception {
        if (entry.getLocalPath() == null || entry.getLocalPath().isBlank()) {
            throw new IllegalStateException(
                    "No local path was recorded for this upload operation. Cannot retry.");
        }

        Path localPath = Paths.get(entry.getLocalPath());
        if (!Files.exists(localPath)) {
            throw new IllegalStateException(
                    "Local mirror file no longer exists at '" + localPath + "'. " +
                            "It may have been cleaned up before the retry. Manual re-upload required.");
        }

        long fileSize = Files.size(localPath);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2Props.bucketName())
                .key(entry.getStorageKey())
                .contentType(entry.getContentType() != null
                        ? entry.getContentType()
                        : "application/octet-stream")
                .contentLength(fileSize)
                .build();

        try (InputStream is = Files.newInputStream(localPath)) {
            s3Client.putObject(request, RequestBody.fromInputStream(is, fileSize));
        }
    }

    private void performDelete(AsyncOperationLog entry) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(r2Props.bucketName())
                .key(entry.getStorageKey())
                .build());
    }

    private void sendRecoveryNotification(AsyncOperationLog entry) {
        if (entry.getOperationType() != AsyncOperationType.UPLOAD)
            return;
        if (entry.getUserId() == null)
            return;

        try {
            User user = userRepository.findById(entry.getUserId()).orElse(null);
            if (user == null)
                return;

            String metadata = String.format(
                    "{\"logId\":\"%s\",\"storageKey\":\"%s\"}", entry.getId(), entry.getStorageKey());

            notificationService.createNotification(
                    user,
                    null,
                    NotificationType.STORAGE_OPERATION_RETRIED,
                    "Your upload has been recovered",
                    "Good news! Your profile picture that previously failed to upload has been " +
                            "successfully recovered by our team. Your profile is now fully up to date.",
                    NotificationPriority.MEDIUM,
                    false,
                    null,
                    metadata);
        } catch (Exception ex) {
            log.error("[R2Retry] Failed to send recovery notification to userId={}: {}",
                    entry.getUserId(), ex.getMessage(), ex);
        }
    }
}
