package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import java.time.Instant;
import java.util.UUID;

public record AdminActionLogResponse(
    UUID id,
    String adminEmail,
    String adminName,
    AdminActionType action,
    String resourceId,
    String actionDetails,
    String ipAddress,
    Instant createdAt
) {}
