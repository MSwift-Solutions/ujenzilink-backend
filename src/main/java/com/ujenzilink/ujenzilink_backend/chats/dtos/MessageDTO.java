package com.ujenzilink.ujenzilink_backend.chats.dtos;

import com.ujenzilink.ujenzilink_backend.chats.enums.MessageStatus;
import com.ujenzilink.ujenzilink_backend.chats.enums.MessageType;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full message details including read receipts for group chats
 */
public record MessageDTO(
        UUID id,
        UUID conversationId,
        CreatorInfoDTO sender,
        String content,
        MessageType messageType,
        MessageStatus status,
        List<ReadByDTO> readBy,
        Instant createdAt) {
    public record ReadByDTO(
            CreatorInfoDTO user,
            Instant readAt) {
    }
}
