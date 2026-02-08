package com.ujenzilink.ujenzilink_backend.chats.dtos;

import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response for creating a group conversation
 */
public record GroupConversationDTO(
                UUID id,
                String name,
                List<ConversationSummaryDTO.ChatUserDTO> participants,
                Instant createdAt) {
}
