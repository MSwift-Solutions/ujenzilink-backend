package com.ujenzilink.ujenzilink_backend.images.repositories;

import com.ujenzilink.ujenzilink_backend.images.enums.AsyncOperationStatus;
import com.ujenzilink.ujenzilink_backend.images.models.AsyncOperationLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AsyncOperationLogRepository extends JpaRepository<AsyncOperationLog, UUID> {

    List<AsyncOperationLog> findByStatusOrderByFailedAtDesc(AsyncOperationStatus status, Pageable pageable);

    List<AsyncOperationLog> findByStatusAndFailedAtBeforeOrderByFailedAtDesc(
            AsyncOperationStatus status, Instant before, Pageable pageable);

    long countByStatus(AsyncOperationStatus status);
}
