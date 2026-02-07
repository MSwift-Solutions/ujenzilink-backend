package com.ujenzilink.ujenzilink_backend.posts.dtos;

import java.util.UUID;

public record CreatePostResponse(
        UUID postId,
        String message) {
}
