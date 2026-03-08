package com.ujenzilink.ujenzilink_backend.search.dtos;

import java.util.UUID;


public record SearchPeopleResult(
        UUID userId,
        String name,
        String username,
        String profilePictureUrl,
        String bio,
        String location,
        String skills,
        String lastActivity) {
}
