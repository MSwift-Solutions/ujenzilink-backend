package com.ujenzilink.ujenzilink_backend.search.dtos;

import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectStatus;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectType;

import java.time.Instant;
import java.util.UUID;

public record SearchProjectResult(
        UUID projectId,
        String title,
        String description,
        ProjectType projectType,
        ProjectStatus projectStatus,
        String location,
        CreatorInfoDTO creator,
        String thumbnailUrl,
        Instant createdAt,
        Instant updatedAt) {
}
