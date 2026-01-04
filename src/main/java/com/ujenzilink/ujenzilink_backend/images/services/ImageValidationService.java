package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import com.ujenzilink.ujenzilink_backend.images.exceptions.ImageValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageValidationService {

    private static final long MAX_FILE_SIZE = 1_572_864L; // 1.5 MB in bytes
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp");

    /**
     * Validates the uploaded image file and extracts metadata.
     * 
     * @param file The multipart file to validate
     * @return ImageMetadata containing extracted information
     * @throws ImageValidationException if validation fails
     */
    public ImageMetadata validateAndExtractMetadata(MultipartFile file) {
        // 1. Check if file is present
        if (file == null || file.isEmpty()) {
            throw new ImageValidationException("Image file is required and cannot be empty.");
        }

        // 2. Get original filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new ImageValidationException("Invalid filename.");
        }

        // 3. Validate file extension
        String lowercaseFilename = originalFilename.toLowerCase();
        boolean hasValidExtension = ALLOWED_EXTENSIONS.stream()
                .anyMatch(lowercaseFilename::endsWith);

        if (!hasValidExtension) {
            throw new ImageValidationException(
                    "Invalid file type. Only JPEG, PNG, and WEBP images are allowed.");
        }

        // 4. Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new ImageValidationException(
                    "Invalid content type. Only JPEG, PNG, and WEBP images are allowed.");
        }

        // 5. Validate file size
        long fileSize = file.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            double sizeMB = fileSize / (1024.0 * 1024.0);
            throw new ImageValidationException(
                    String.format("File size (%.2f MB) exceeds the maximum allowed size of 1.5 MB.", sizeMB));
        }

        // 6. Validate actual image content and extract dimensions
        BufferedImage image;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new ImageValidationException("Failed to read image file. The file may be corrupted.", e);
        }

        if (image == null) {
            throw new ImageValidationException(
                    "Invalid image file. The file is not a valid image or the format is not supported.");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // 7. Validate dimensions
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            throw new ImageValidationException(
                    String.format(
                            "Image dimensions (%dx%d) exceed the maximum allowed size of %dx%d.",
                            width, height, MAX_WIDTH, MAX_HEIGHT));
        }

        // 8. Return metadata
        return new ImageMetadata(
                originalFilename,
                contentType,
                fileSize,
                width,
                height);
    }
}
