package com.ujenzilink.ujenzilink_backend.projects.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectPrivacyAdminRequest(
    @NotNull(message = "makePrivate status is required")
    Boolean makePrivate,

    @NotBlank(message = "reason is required")
    String reason
) {}
