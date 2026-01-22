package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.util.UUID;

public record CreatorInfoDTO(
        UUID userId,
        String name,
        String username,
        String profilePictureUrl) {
}
