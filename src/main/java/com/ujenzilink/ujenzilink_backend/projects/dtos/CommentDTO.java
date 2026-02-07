package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentDTO(
        UUID id,
        CreatorInfoDTO user,
        String text,
        Instant timestamp,
        boolean hasLiked,
        int likesCount,
        List<ReplyDTO> replies) {
}
