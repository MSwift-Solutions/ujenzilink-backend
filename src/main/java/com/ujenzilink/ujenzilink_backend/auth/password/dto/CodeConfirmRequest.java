package com.ujenzilink.ujenzilink_backend.auth.password.dto;

import jakarta.validation.constraints.NotBlank;

public record CodeConfirmRequest(
        @NotBlank(message = "Reset code is required") String resetCode) {
}
