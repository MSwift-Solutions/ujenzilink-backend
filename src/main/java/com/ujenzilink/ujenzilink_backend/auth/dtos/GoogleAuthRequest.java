package com.ujenzilink.ujenzilink_backend.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "ID token is required") String idToken) {
}
