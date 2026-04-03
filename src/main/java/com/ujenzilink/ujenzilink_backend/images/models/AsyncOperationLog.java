package com.ujenzilink.ujenzilink_backend.images.models;

import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationStatus;
import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "async_operation_logs", indexes = {
        @Index(name = "idx_aol_status", columnList = "status"),
        @Index(name = "idx_aol_user_id", columnList = "user_id"),
        @Index(name = "idx_aol_failed_at", columnList = "failed_at")
})
public class AsyncOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AsyncOperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AsyncOperationStatus status = AsyncOperationStatus.FAILED;

    /** R2 object key (e.g. profile-pictures/123/avatar-uuid.jpg). */
    @Column(nullable = false, length = 500)
    private String storageKey;

    /**
     * Absolute path to the local mirror file — populated only for UPLOAD
     * operations.
     */
    @Column(length = 1000)
    private String localPath;

    @Column(length = 255)
    private String contentType;

    @Column(name = "user_id")
    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "failed_at", updatable = false, nullable = false)
    private Instant failedAt;

    @Column
    private Instant retriedAt;

    @Column
    private Instant resolvedAt;

    @Column(nullable = false)
    private int retryCount = 0;

    public AsyncOperationLog() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AsyncOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(AsyncOperationType operationType) {
        this.operationType = operationType;
    }

    public AsyncOperationStatus getStatus() {
        return status;
    }

    public void setStatus(AsyncOperationStatus status) {
        this.status = status;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public Instant getRetriedAt() {
        return retriedAt;
    }

    public void setRetriedAt(Instant retriedAt) {
        this.retriedAt = retriedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
