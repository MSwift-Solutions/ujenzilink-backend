package com.ujenzilink.ujenzilink_backend.images.dtos;

import java.time.Instant;

public record R2UploadResponse(String fileName,
                               String key,
                               String url,
                               String contentType,
                               long size,
                               String eTag,
                               Instant uploadedAt) {
}
