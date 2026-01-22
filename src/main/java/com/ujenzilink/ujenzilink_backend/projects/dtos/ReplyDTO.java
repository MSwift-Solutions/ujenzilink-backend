package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.time.Instant;
import java.util.UUID;

public record ReplyDTO(
        UUID id,
        CreatorInfoDTO user,
        String text,
        Instant timestamp,
        boolean hasLiked,
        int likesCount) {
}
