package com.ujenzilink.ujenzilink_backend.chats.dtos;

import java.time.Instant;
import java.util.UUID;

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
