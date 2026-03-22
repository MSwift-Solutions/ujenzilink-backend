package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import java.time.Instant;
import java.util.UUID;

public record UserDeletionRequestResponse(
        UUID id,
        String fullName,
        String email,
        String username,
        Instant deletedAt,
        String profilePictureUrl
) {
}
