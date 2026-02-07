package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.util.UUID;

public record CreateProjectStageResponse(
        UUID stageId,
        String message) {
}
