package com.ujenzilink.ujenzilink_backend.images.services;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Service
public class ImageOptimizationService {

    private static final int MAX_WIDTH = 2000;
    private static final int MAX_HEIGHT = 2000;
    private static final float JPEG_QUALITY = 0.85f;

    private final ImageValidationService imageValidationService;

    public ImageOptimizationService(ImageValidationService imageValidationService) {
        this.imageValidationService = imageValidationService;
    }

    /**
     * Optimizes the image and writes it to a temporary file on disk.
     * The caller MUST delete the returned Path when finished (use a try/finally).
     * This method never loads the full image into JVM heap memory.
     */
    public Path optimizeToTempFile(MultipartFile file) {
        imageValidationService.validateAndExtractMetadata(file);

        String contentType = file.getContentType();
        if (contentType == null) {
            return writeOriginalToTempFile(file);
        }

        String format = resolveFormat(contentType.toLowerCase(Locale.ROOT));

        // If unsupported (e.g. HEIC), write original bytes directly to disk
        if (format == null) {
            return writeOriginalToTempFile(file);
        }

        try {
            Path tempFile = Files.createTempFile("img-opt-", "." + format);

            try (InputStream input = file.getInputStream();
                 OutputStream output = Files.newOutputStream(tempFile)) {

                Thumbnails.Builder<?> builder = Thumbnails.of(input)
                        .size(MAX_WIDTH, MAX_HEIGHT)
                        .outputFormat(format);

                if ("jpg".equals(format)) {
                    builder.outputQuality(JPEG_QUALITY);
                }

                builder.toOutputStream(output);
            }

            return tempFile;

        } catch (IOException e) {
            throw new RuntimeException("Image optimization failed", e);
        }
    }

    private String resolveFormat(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> null;
        };
    }

    /**
     * Streams the original file bytes to a temp file without any RAM buffering.
     */
    private Path writeOriginalToTempFile(MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("img-orig-", ".tmp");
            try (InputStream input = file.getInputStream();
                 OutputStream output = Files.newOutputStream(tempFile)) {
                input.transferTo(output);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write original image to temp file", e);
        }
    }
}
