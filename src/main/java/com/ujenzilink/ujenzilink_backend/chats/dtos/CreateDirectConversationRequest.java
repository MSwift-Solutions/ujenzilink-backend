package com.ujenzilink.ujenzilink_backend.chats.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request to create a new direct (one-on-one) conversation
 */
public record CreateDirectConversationRequest(
        @NotNull(message = "Participant ID is required") UUID participantId) {
}
