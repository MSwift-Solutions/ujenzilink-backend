package com.ujenzilink.ujenzilink_backend.search.dtos;

import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public record SearchPostResult(
        UUID postId,
        String content,
        CreatorInfoDTO creator,
        List<String> images,
        Integer likesCount,
        Integer commentsCount,
        Instant createdAt,
        Instant updatedAt) {
}
