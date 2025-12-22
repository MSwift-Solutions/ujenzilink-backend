package com.ujenzilink.ujenzilink_backend.auth.password.dto;

import com.ujenzilink.ujenzilink_backend.auth.validators.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPassResetNew(
                @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

                @NotBlank(message = "New password is required") @Password String newPassword,

                @NotBlank(message = "Password confirmation is required") String confirmPassword) {
}
