package com.ujenzilink.ujenzilink_backend.chats.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request to create a new conversation (direct or group)
 */
public record CreateConversationRequest(
        String name, // Required for groups, ignored for direct chats

        @NotNull(message = "isGroup flag is required") Boolean isGroup,

        @NotEmpty(message = "At least one participant is required") List<UUID> participantIds) {
}
