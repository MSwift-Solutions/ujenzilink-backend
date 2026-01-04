package com.ujenzilink.ujenzilink_backend.images.dtos;

import jakarta.validation.constraints.NotBlank;

public record ProfilePictureRequest(
                @NotBlank(message = "Profile picture cannot be null or empty!") String profilePicture,
                String filename,
                String fileType,
                Long fileSize,
                Integer width,
                Integer height) {
}
