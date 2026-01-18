package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectStatus;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectListResponse(
        UUID projectId,
        String projectName,
        ProjectType projectType,
        ProjectStatus projectStatus,
        String location,
        Instant createdAt,
        CreatorInfoDTO creator,
        Integer memberCount,
        List<String> projectImages,
        BigDecimal estimatedBudget,
        String currency,
        Integer likesCount,
        Integer commentsCount,
        String currentStage) {
}
