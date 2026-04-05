package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminPostResponse(
    UUID postId,
    String content,
    Instant createdAt,
    CreatorInfoDTO creator,
    List<String> images,
    Integer likesCount,
    Integer commentsCount,
    Integer views,
    Integer impressions,
    boolean isDeleted,
    String adminDeletionReason,
    String deletedByAdminHandle
) {}
