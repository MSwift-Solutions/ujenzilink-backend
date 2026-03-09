package com.ujenzilink.ujenzilink_backend.search.dtos;

import java.util.UUID;

public record PersonDTO(
        UUID id,
        String name,
        String username,
        String avatar,
        String role,
        String location,
        int projectsCount,
        int postsCount,
        String bio,
        int yearsInConstruction) {
}
