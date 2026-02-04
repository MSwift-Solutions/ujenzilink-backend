package com.ujenzilink.ujenzilink_backend.posts.dtos;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record EditPostRequest(
        @Size(max = 5000, message = "Content must not exceed 5000 characters") String content,

        List<UUID> removedImageIds // Image IDs to mark as deleted
) {
}
