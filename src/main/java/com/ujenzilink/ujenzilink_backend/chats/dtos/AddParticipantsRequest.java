package com.ujenzilink.ujenzilink_backend.chats.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Request to add participants to a group conversation
 */
public record AddParticipantsRequest(
        @NotEmpty(message = "At least one user ID is required") List<UUID> userIds) {
}
