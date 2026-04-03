package com.ujenzilink.ujenzilink_backend.images.dtos;

import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationStatus;
import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationType;

import java.time.Instant;
import java.util.UUID;

public record AsyncOperationLogDTO(
        UUID id,
        AsyncOperationType operationType,
        AsyncOperationStatus status,
        String storageKey,
        String localPath,
        String contentType,
        UUID userId,
        String errorMessage,
        Instant failedAt,
        Instant retriedAt,
        Instant resolvedAt,
        int retryCount
) {}
