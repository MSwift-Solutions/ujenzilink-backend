package com.ujenzilink.ujenzilink_backend.projects.dtos;

public record ProjectStatsDTO(
        Integer workers,
        Integer progress,
        Integer impressions,
        Integer posts) {
}
