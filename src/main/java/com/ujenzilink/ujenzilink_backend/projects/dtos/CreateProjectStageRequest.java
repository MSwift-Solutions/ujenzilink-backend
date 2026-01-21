package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.ConstructionStage;
import com.ujenzilink.ujenzilink_backend.projects.enums.PostType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateProjectStageRequest(
        @NotNull(message = "Project ID is required") UUID projectId,

        @Size(max = 1000, message = "Description must not exceed 1000 characters") String description,

        @NotNull(message = "Construction stage is required") ConstructionStage constructionStage,

        PostType postType,

        String visibility,

        BigDecimal stageCost,

        Integer totalWorkers,

        String materialsUsed,

        LocalDate endDate) {
}
