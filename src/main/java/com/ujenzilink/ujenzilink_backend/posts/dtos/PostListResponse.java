package com.ujenzilink.ujenzilink_backend.posts.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostListResponse(
        UUID postId,
        String content,
        Instant createdAt,
        Instant updatedAt,
        boolean isEdited,
        CreatorInfoDTO creator,
        List<String> images,
        Integer likesCount,
        Integer commentsCount,
        Integer bookmarksCount,
        Integer views) {
}
