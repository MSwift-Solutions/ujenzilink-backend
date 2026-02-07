package com.ujenzilink.ujenzilink_backend.projects.dtos;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateCommentRequest(
        @NotBlank(message = "Comment text is required") String text,
        UUID parentId) {
}
