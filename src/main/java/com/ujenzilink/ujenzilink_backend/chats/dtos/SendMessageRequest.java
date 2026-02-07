package com.ujenzilink.ujenzilink_backend.chats.dtos;

import com.ujenzilink.ujenzilink_backend.chats.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to send a message
 */
public record SendMessageRequest(
        @NotBlank(message = "Message content cannot be empty") String content,

        @NotNull(message = "Message type is required") MessageType messageType) {
}
