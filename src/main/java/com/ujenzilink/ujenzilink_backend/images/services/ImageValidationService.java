package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;

@Service
public class ImageValidationService {

    @Value("${app.upload.max-file-size:52428800}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/heic");

    public ImageMetadata validateAndExtractMetadata(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }

        // 1. Fast Size Check (No CPU overhead)
        if (file.getSize() > maxFileSize) {
            long limitInMb = maxFileSize / (1024 * 1024);
            throw new IllegalArgumentException("File exceeds the " + limitInMb + "MB limit.");
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
