package com.ujenzilink.ujenzilink_backend.posts.dtos;

import jakarta.validation.constraints.Size;

public record CreatePostRequest(
                @Size(max = 5000, message = "Content must not exceed 5000 characters") String content) {
}
