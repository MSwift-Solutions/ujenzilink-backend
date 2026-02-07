package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.time.Instant;

public record ProjectLikeDTO(
        CreatorInfoDTO user,
        Instant createdAt) {
}
