package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.util.List;

public record ProjectDetailsResponse(
        String overview,
        ProjectStatsDTO stats,
        List<ConstructionStageDTO> constructionStages) {
}
