package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import jakarta.validation.constraints.NotBlank;

public record DeletePostAdminRequest(
    @NotBlank(message = "Reason for deletion is required")
    String reason
) {}
