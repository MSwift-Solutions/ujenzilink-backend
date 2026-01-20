package com.ujenzilink.ujenzilink_backend.projects.dtos;

public record ConstructionStageDTO(
        String stageName,
        boolean completed,
        boolean active,
        boolean upcoming) {
}
