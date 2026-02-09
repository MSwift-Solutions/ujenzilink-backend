package com.ujenzilink.ujenzilink_backend.notifications.dtos;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationDTO> notifications,
        String nextCursor,
        boolean hasMore,
        int unreadCount) {
}
