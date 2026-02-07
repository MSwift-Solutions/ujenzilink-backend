package com.ujenzilink.ujenzilink_backend.chats.dtos;

import com.ujenzilink.ujenzilink_backend.chats.enums.ParticipantRole;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full conversation details with participants
 */
public record ConversationDTO(
        UUID id,
        String name,
        boolean isGroup,
        UUID projectId,
        CreatorInfoDTO createdBy,
        List<ParticipantDTO> participants,
        Instant createdAt,
        Instant updatedAt) {
    public record ParticipantDTO(
            CreatorInfoDTO user,
            ParticipantRole role,
            Instant joinedAt,
            Instant leftAt) {
    }
}
