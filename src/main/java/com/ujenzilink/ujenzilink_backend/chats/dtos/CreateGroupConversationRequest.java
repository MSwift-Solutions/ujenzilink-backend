package com.ujenzilink.ujenzilink_backend.chats.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Request to create a new group conversation
 */
public record CreateGroupConversationRequest(
        @NotNull(message = "Group name is required") String name,
        @NotEmpty(message = "At least one participant is required") List<UUID> participantIds) {
}
