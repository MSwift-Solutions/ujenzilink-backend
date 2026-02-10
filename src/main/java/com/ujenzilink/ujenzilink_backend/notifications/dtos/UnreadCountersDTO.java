package com.ujenzilink.ujenzilink_backend.notifications.dtos;

public record UnreadCountersDTO(
        boolean hasUnreadMessages,
        boolean hasUnreadNotifications) {
}
