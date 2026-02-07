package com.ujenzilink.ujenzilink_backend.chats.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to edit a message
 */
public record EditMessageRequest(
        @NotBlank(message = "Message content cannot be empty") String content) {
}
