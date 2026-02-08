package com.ujenzilink.ujenzilink_backend.chats.dtos;

import java.time.Instant;
import java.util.UUID;

/**
 * Response for creating a direct conversation
 */
public record DirectConversationDTO(
        UUID id,
        ConversationSummaryDTO.ChatUserDTO otherParticipant,
        Instant createdAt) {
}
