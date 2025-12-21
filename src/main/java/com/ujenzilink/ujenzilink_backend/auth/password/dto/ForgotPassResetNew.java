package com.ujenzilink.ujenzilink_backend.auth.password.dto;

import com.ujenzilink.ujenzilink_backend.auth.validators.Password;
import jakarta.validation.constraints.NotBlank;

public record ForgotPassResetNew(
        @NotBlank(message = "Reset code is required") String resetCode,

        @NotBlank(message = "New password is required") @Password String newPassword,

        @NotBlank(message = "Password confirmation is required") String confirmPassword) {
}
