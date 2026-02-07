package com.ujenzilink.ujenzilink_backend.chats.dtos;

import java.time.Instant;
import java.util.UUID;

/**
 * Summary of a conversation for the chat home screen
 * Shows: name, last message, unread count, participants (for direct chats)
 */
public record ConversationSummaryDTO(
                UUID id,
                ChatUserDTO user,
                String lastMessage,
                int unreadCount,
                Instant updatedAt,
                boolean isGroup) {

        public record ChatUserDTO(
                        String name,
                        String username,
                        String avatar,
                        boolean isOnline) {
        }
}
