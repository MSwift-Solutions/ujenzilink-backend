package com.ujenzilink.ujenzilink_backend.images.dtos;

import jakarta.validation.constraints.NotBlank;

public record ProfilePictureRequest(
        @NotBlank(message = "Profile picture cannot be null or empty!") String profilePicture) {
}

