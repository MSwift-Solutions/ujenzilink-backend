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
    private static final float IMAGE_QUALITY = 0.90f;

    public ImageOptimizationService() {
    }

    /** Optimizes the image and writes it to a temporary file on disk. */
    public Path optimizeToTempFile(MultipartFile file) {
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
            doOptimize(file, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Image optimization failed", e);
        }
    }

    /** Optimizes the image and writes it to {@code targetPath}. */
    public void optimizeToPath(MultipartFile file, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            doOptimize(file, targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Image optimization failed writing to " + targetPath, e);
        }
    }


    private void doOptimize(MultipartFile file, Path targetPath) throws IOException {
        String contentType = file.getContentType();
        String format = (contentType != null)
                ? resolveFormat(contentType.toLowerCase(Locale.ROOT))
                : null;

        try (InputStream input = file.getInputStream();
             OutputStream output = Files.newOutputStream(targetPath)) {

            if (format == null) {
                input.transferTo(output);
                return;
            }

            Thumbnails.Builder<?> builder = Thumbnails.of(input)
                    .size(MAX_WIDTH, MAX_HEIGHT)
                    .outputFormat(format);

            if ("jpg".equals(format) || "webp".equals(format)) {
                builder.outputQuality(IMAGE_QUALITY);
            }

            builder.toOutputStream(output);
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

    /** Streams the original file bytes to a temp file. */
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
