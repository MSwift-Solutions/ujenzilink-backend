package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.time.Instant;
import java.util.UUID;

public record ProjectFollowDTO(
        UUID id,
        UUID projectId,
        CreatorInfoDTO follower,
        Instant createdAt,
        Instant endDate,
        boolean notificationsEnabled,
        boolean isActive) {
}
