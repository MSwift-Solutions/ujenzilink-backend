package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectVisibility;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectVisibilityRequest(
        @NotNull(message = "Visibility is required") ProjectVisibility visibility) {
}
