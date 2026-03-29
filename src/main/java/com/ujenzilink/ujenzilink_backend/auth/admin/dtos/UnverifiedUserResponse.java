package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import java.time.Instant;
import java.util.UUID;

public record UnverifiedUserResponse(
        UUID id,
        String fullName,
        String email,
        String username,
        Instant registeredAt,
        String profilePictureUrl
) {
}
