package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectStatus;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectType;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EditProjectRequest(
        @Size(max = 255, message = "Title must not exceed 255 characters") String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,

        ProjectType projectType,

        ProjectStatus projectStatus,

        @Size(max = 500, message = "Location must not exceed 500 characters") String location,

        LocalDate startDate,

        LocalDate expectedEndDate,

        BigDecimal estimatedBudget,

        BigDecimal contractValue,

        @Size(max = 3, message = "Currency code must be 3 characters (ISO 4217)") String currency) {
}
