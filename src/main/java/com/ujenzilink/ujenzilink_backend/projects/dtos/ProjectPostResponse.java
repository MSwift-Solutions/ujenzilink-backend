package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectPostResponse(
        UUID id,
        String description,
        String constructionStage,
        String postType,
        String visibility,
        BigDecimal stageCost,
        Integer totalWorkers,
        String materialsUsed,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        CreatorInfoDTO postedBy,
        List<String> images,
        Integer commentsCount,
        Integer likesCount) {
}
