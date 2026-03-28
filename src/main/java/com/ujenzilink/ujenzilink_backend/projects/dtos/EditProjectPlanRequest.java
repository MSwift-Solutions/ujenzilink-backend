package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.PlanVisibility;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EditProjectPlanRequest(
        @Size(max = 255, message = "Name must not exceed 255 characters") String name,
        @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,
        BigDecimal price,
        PlanVisibility visibility
) {
}
