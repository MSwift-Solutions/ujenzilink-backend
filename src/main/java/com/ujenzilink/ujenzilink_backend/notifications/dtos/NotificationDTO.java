package com.ujenzilink.ujenzilink_backend.notifications.dtos;

import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;

import java.time.Instant;
import java.util.UUID;

public record NotificationDTO(
        UUID id,
        NotificationType type,
        String title,
        String message,
        NotificationPriority priority,
        CreatorInfoDTO initiator,
        boolean isRead,
        Instant readAt,
        boolean isBatched,
        Integer aggregationCount,
        Instant createdAt) {
}
