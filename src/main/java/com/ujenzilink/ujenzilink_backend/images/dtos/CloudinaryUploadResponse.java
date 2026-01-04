package com.ujenzilink.ujenzilink_backend.images.dtos;

import java.util.Map;

public record CloudinaryUploadResponse(
        String url,
        String secureUrl,
        String publicId,
        String format,
        Integer width,
        Integer height,
        Long bytes) {
    public static CloudinaryUploadResponse from(Map<?, ?> uploadResult) {
        return new CloudinaryUploadResponse(
                (String) uploadResult.get("url"),
                (String) uploadResult.get("secure_url"),
                (String) uploadResult.get("public_id"),
                (String) uploadResult.get("format"),
                (Integer) uploadResult.get("width"),
                (Integer) uploadResult.get("height"),
                uploadResult.get("bytes") instanceof Integer ? ((Integer) uploadResult.get("bytes")).longValue()
                        : (Long) uploadResult.get("bytes"));
    }
}
