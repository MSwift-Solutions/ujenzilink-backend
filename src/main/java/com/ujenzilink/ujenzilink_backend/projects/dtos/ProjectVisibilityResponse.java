package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectVisibility;

public record ProjectVisibilityResponse(
        ProjectVisibility visibility,
        String formattedVisibility) {
}
