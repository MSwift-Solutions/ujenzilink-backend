package com.ujenzilink.ujenzilink_backend.legal.dtos;

import java.time.Instant;

public record LegalDocumentDto(
        String title,
        String version,
        String content,
        Instant lastUpdatedAt
) {}
