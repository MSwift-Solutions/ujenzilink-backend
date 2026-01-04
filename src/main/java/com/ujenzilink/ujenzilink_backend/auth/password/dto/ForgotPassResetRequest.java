package com.ujenzilink.ujenzilink_backend.auth.password.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPassResetRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
}
