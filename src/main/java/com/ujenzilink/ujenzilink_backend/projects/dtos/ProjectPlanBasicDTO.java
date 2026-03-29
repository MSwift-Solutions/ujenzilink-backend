package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ProjectPlanBasicDTO(
        UUID id,
        String title,
        BigDecimal price
) {
}
