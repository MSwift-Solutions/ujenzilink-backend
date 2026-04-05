package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectStatus;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectType;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectVisibility;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminProjectResponse(
        UUID projectId,
        String title,
        String description,
        ProjectType projectType,
        ProjectStatus projectStatus,
        ProjectVisibility visibility,
        String location,
        Instant createdAt,
        CreatorInfoDTO owner,
        BigDecimal estimatedBudget,
        BigDecimal contractValue,
        String currency,
        boolean isAdminPrivate,
        String adminPrivateReason,
        String adminPrivateByUsername
) {}
