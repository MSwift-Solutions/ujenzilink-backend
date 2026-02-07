package com.ujenzilink.ujenzilink_backend.chats.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to update group conversation name
 */
public record UpdateGroupNameRequest(
        @NotBlank(message = "Group name cannot be empty") String name) {
}
