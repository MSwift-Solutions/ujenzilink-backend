package com.ujenzilink.ujenzilink_backend.chats.dtos;

import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;

import java.time.Instant;
import java.util.UUID;

/**
 * Summary of a conversation for the chat home screen
 * Shows: name, last message, unread count, participants (for direct chats)
 */
public record ConversationSummaryDTO(
        UUID id,
        String name,
        boolean isGroup,
        UUID projectId,
        LastMessageDTO lastMessage,
        int unreadCount,
        int participantCount,
        CreatorInfoDTO otherParticipant, // For direct chats only
        Instant createdAt,
        Instant updatedAt) {
    public record LastMessageDTO(
            String content,
            CreatorInfoDTO sender,
            Instant createdAt) {
    }
}
