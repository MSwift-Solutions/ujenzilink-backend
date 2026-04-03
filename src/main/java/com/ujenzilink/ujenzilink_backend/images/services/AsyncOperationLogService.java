package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.images.dtos.AsyncOperationLogDTO;
import com.ujenzilink.ujenzilink_backend.images.dtos.AsyncOperationLogPageResponse;
import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationStatus;
import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationType;
import com.ujenzilink.ujenzilink_backend.images.models.AsyncOperationLog;
import com.ujenzilink.ujenzilink_backend.images.repositories.AsyncOperationLogRepository;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class AsyncOperationLogService {

    private static final Logger log = LoggerFactory.getLogger(AsyncOperationLogService.class);

    private final AsyncOperationLogRepository repository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public AsyncOperationLogService(
            AsyncOperationLogRepository repository,
            NotificationService notificationService,
            UserRepository userRepository) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public AsyncOperationLog recordFailure(
            AsyncOperationType type,
            String storageKey,
            Path localPath,
            String contentType,
            UUID userId,
            String errorMessage) {

        log.error("[AsyncOp] {} FAILED — key='{}' userId='{}' reason='{}'",
                type, storageKey, userId, errorMessage);

        AsyncOperationLog entry = new AsyncOperationLog();
        entry.setOperationType(type);
        entry.setStatus(AsyncOperationStatus.FAILED);
        entry.setStorageKey(storageKey);
        entry.setLocalPath(localPath != null ? localPath.toAbsolutePath().toString() : null);
        entry.setContentType(contentType);
        entry.setUserId(userId);
        entry.setErrorMessage(errorMessage);

        AsyncOperationLog saved = repository.save(entry);

        // Only notify user for upload failures — delete failures are transparent to UX
        if (type == AsyncOperationType.UPLOAD && userId != null) {
            sendUploadFailureNotification(userId, storageKey, saved.getId());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public AsyncOperationLogPageResponse getFailures(AsyncOperationStatus status, String cursor, Integer size) {
        if (size == null || size < 1)
            size = 20;
        if (size > 100)
            size = 100;

        Instant cursorTime = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(cursor));
                // Cursor JSON: {"timestamp":"2024-01-01T00:00:00Z"}
                cursorTime = Instant.parse(
                        decoded.replaceAll(".*\"timestamp\":\"([^\"]+)\".*", "$1"));
            } catch (Exception ignored) {
                log.warn("[AsyncOpLog] Invalid cursor supplied — ignoring and returning from start");
            }
        }

        Pageable pageable = PageRequest.of(0, size + 1);
        List<AsyncOperationLog> logs;

        if (cursorTime != null) {
            logs = repository.findByStatusAndFailedAtBeforeOrderByFailedAtDesc(status, cursorTime, pageable);
        } else {
            logs = repository.findByStatusOrderByFailedAtDesc(status, pageable);
        }

        boolean hasMore = logs.size() > size;
        if (hasMore)
            logs = logs.subList(0, size);

        String nextCursor = null;
        if (hasMore && !logs.isEmpty()) {
            String cursorJson = String.format("{\"timestamp\":\"%s\"}",
                    logs.get(logs.size() - 1).getFailedAt());
            nextCursor = Base64.getEncoder().encodeToString(cursorJson.getBytes());
        }

        long totalFailed = repository.countByStatus(AsyncOperationStatus.FAILED);
        long totalRetried = repository.countByStatus(AsyncOperationStatus.RETRIED);

        List<AsyncOperationLogDTO> dtos = logs.stream().map(this::toDTO).toList();

        return new AsyncOperationLogPageResponse(dtos, nextCursor, hasMore,
                (int) totalFailed, (int) totalRetried);
    }

    public AsyncOperationLogDTO toDTO(AsyncOperationLog entry) {
        return new AsyncOperationLogDTO(
                entry.getId(),
                entry.getOperationType(),
                entry.getStatus(),
                entry.getStorageKey(),
                entry.getLocalPath(),
                entry.getContentType(),
                entry.getUserId(),
                entry.getErrorMessage(),
                entry.getFailedAt(),
                entry.getRetriedAt(),
                entry.getResolvedAt(),
                entry.getRetryCount());
    }

    private void sendUploadFailureNotification(UUID userId, String storageKey, UUID logId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null)
                return;

            String metadata = String.format("{\"logId\":\"%s\",\"storageKey\":\"%s\"}", logId, storageKey);

            notificationService.createNotification(
                    user,
                    null,
                    NotificationType.STORAGE_UPLOAD_FAILED,
                    "Profile picture upload failed",
                    "We were unable to save your profile picture to cloud storage. " +
                            "Our team has been alerted and will recover your upload shortly.",
                    NotificationPriority.HIGH,
                    false,
                    null,
                    metadata);
        } catch (Exception ex) {
            log.error("[AsyncOpLog] Failed to send upload-failure notification to userId={}: {}",
                    userId, ex.getMessage(), ex);
        }
    }
}
