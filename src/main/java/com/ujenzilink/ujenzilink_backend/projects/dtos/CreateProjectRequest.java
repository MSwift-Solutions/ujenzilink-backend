package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectStatus;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectType;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectVisibility;
import com.ujenzilink.ujenzilink_backend.projects.enums.BudgetVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProjectRequest(
        // Mandatory fields
        @NotBlank(message = "Project title cannot be null or empty!") @Size(max = 255, message = "Title must not exceed 255 characters") String title,

        @NotNull(message = "Project type is required!") ProjectType projectType,

        // Optional fields with defaults
        @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,

        ProjectStatus projectStatus, // Optional, defaults to PLANNING in service

        ProjectVisibility visibility, // Optional, defaults to PRIVATE in service

        @Size(max = 500, message = "Location must not exceed 500 characters") String location,

        LocalDate startDate,

        LocalDate expectedEndDate,

        BigDecimal estimatedBudget,

        BigDecimal contractValue,

        @Size(max = 3, message = "Currency code must be 3 characters (ISO 4217)") String currency,

        BudgetVisibility budgetVisibility) {
}
