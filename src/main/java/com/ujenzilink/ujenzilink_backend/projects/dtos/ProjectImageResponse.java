package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.time.Instant;

public record ProjectImageResponse(
        String url,
        Instant uploadedAt,
        String imageName) {
}
