package com.ujenzilink.ujenzilink_backend.images.services;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public byte[] optimize(MultipartFile file) {
        imageValidationService.validateAndExtractMetadata(file);

        String contentType = file.getContentType();
        if (contentType == null) {
            return getOriginalBytes(file);
        }

        String format = resolveFormat(contentType.toLowerCase(Locale.ROOT));

        // If unsupported (e.g. HEIC), return original
        if (format == null) {
            return getOriginalBytes(file);
        }

        try (InputStream input = file.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Thumbnails.Builder<?> builder = Thumbnails.of(input)
                    .size(MAX_WIDTH, MAX_HEIGHT)
                    .outputFormat(format);

            if ("jpg".equals(format)) {
                builder.outputQuality(JPEG_QUALITY);
            }

            builder.toOutputStream(output);
            return output.toByteArray();

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

    private byte[] getOriginalBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image bytes", e);
        }
    }
}
