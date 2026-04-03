package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSuspensionRequest(
        @NotBlank(message = "Reason is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
