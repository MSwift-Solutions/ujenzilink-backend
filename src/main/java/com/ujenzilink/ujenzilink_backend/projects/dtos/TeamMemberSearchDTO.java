package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.util.UUID;

public record TeamMemberSearchDTO(
        UUID userId,
        String name,
        String username,
        String profilePictureUrl,
        String status,
        String lastActivity) {
}
