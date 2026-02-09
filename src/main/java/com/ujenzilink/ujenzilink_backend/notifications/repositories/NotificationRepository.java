package com.ujenzilink.ujenzilink_backend.notifications.repositories;

import com.ujenzilink.ujenzilink_backend.notifications.models.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Cursor pagination methods
    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Notification> findByUser_IdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID userId, Instant cursor,
            Pageable pageable);

    // Count unread notifications
    long countByUser_IdAndReadAtIsNull(UUID userId);
}
