package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;

@Service
public class ImageValidationService {

    private static final long MAX_FILE_SIZE = 52_428_800L; // 50MB - Cloudinary/R2 can handle it
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/heic");

    public ImageMetadata validateAndExtractMetadata(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }

        // 1. Fast Size Check (No CPU overhead)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds the 10MB limit.");
        }

        // 2. MIME Type Check (Security)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image format. Allowed: JPEG, PNG, WEBP, HEIC.");
        }

        return new ImageMetadata(
                file.getOriginalFilename(),
                contentType,
                file.getSize());
    }
}
