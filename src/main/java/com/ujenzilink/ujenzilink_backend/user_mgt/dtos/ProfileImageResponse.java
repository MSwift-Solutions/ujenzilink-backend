package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

import java.time.Instant;
import java.util.UUID;

public record ProfileImageResponse(
        UUID id,
        String url,
        String filename,
        String fileType,
        Long fileSize,
        Integer width,
        Integer height,
        Instant uploadedAt) {
}
