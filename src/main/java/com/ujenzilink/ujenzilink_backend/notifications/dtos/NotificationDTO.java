package com.ujenzilink.ujenzilink_backend.notifications.dtos;

import java.time.Instant;

public record NotificationDTO(
        String title,
        String message,
        boolean isRead,
        Instant createdAt) {
}
