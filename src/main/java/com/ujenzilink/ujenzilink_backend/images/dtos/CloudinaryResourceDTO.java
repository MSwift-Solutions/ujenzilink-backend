package com.ujenzilink.ujenzilink_backend.images.dtos;

import java.time.Instant;

public record CloudinaryResourceDTO(
        String publicId,
        String url,
        String format,
        Long size,
        Instant createdAt,
        String reason
) {}
