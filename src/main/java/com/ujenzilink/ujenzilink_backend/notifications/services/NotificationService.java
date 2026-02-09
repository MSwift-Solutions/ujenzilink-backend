package com.ujenzilink.ujenzilink_backend.notifications.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.notifications.dtos.NotificationDTO;
import com.ujenzilink.ujenzilink_backend.notifications.dtos.NotificationPageResponse;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationStatus;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.models.Notification;
import com.ujenzilink.ujenzilink_backend.notifications.repositories.NotificationRepository;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationLogService logService;

    // Create a new notification
    @Transactional
    public Notification createNotification(
            User recipient,
            User initiator,
            NotificationType type,
            String title,
            String message,
            NotificationPriority priority,
            boolean isBatched,
            String batchKey,
            String metadata) {

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setInitiator(initiator);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setPriority(priority != null ? priority : NotificationPriority.MEDIUM);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setBatched(isBatched);
        notification.setBatchKey(batchKey);
        notification.setMetadata(metadata);

        Notification saved = notificationRepository.save(notification);

        // Log creation
        logService.logNotificationEvent(saved, null, "INFO", "CREATED", "", null);

        return saved;
    }

    // Fetch notifications with cursor pagination
    @Transactional
    public ApiCustomResponse<NotificationPageResponse> getNotifications(User user, String cursor, Integer size) {
        if (size == null || size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }

        Instant cursorTime = null;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decodedJson = new String(java.util.Base64.getDecoder().decode(cursor));
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> cursorData = mapper.readValue(decodedJson, Map.class);
                String timestamp = (String) cursorData.get("timestamp");
                cursorTime = Instant.parse(timestamp);
            } catch (Exception e) {
                return new ApiCustomResponse<>(null, "Invalid cursor format", HttpStatus.BAD_REQUEST.value());
            }
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, size + 1, sort);

        List<Notification> notifications;
        if (cursorTime != null) {
            notifications = notificationRepository.findByUser_IdAndCreatedAtBeforeOrderByCreatedAtDesc(
                    user.getId(), cursorTime, pageable);
        } else {
            notifications = notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable);
        }

        boolean hasMore = notifications.size() > size;
        if (hasMore) {
            notifications = notifications.subList(0, size);
        }

        // Mark fetched notifications as read
        for (Notification notification : notifications) {
            if (notification.getReadAt() == null) {
                notification.setReadAt(Instant.now());
                notification.setStatus(NotificationStatus.READ);
                notificationRepository.save(notification);

                // Log read event
                logService.logNotificationEvent(notification, null, "INFO", "READ", "", null);
            }
        }

        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(this::mapToNotificationDTO)
                .toList();

        String nextCursor = null;
        if (hasMore && !notifications.isEmpty()) {
            try {
                Notification lastNotification = notifications.get(notifications.size() - 1);
                String cursorJson = String.format("{\"timestamp\":\"%s\"}", lastNotification.getCreatedAt().toString());
                nextCursor = java.util.Base64.getEncoder().encodeToString(cursorJson.getBytes());
            } catch (Exception e) {
                // Ignore cursor generation errors
            }
        }

        // Get unread count
        int unreadCount = (int) notificationRepository.countByUser_IdAndReadAtIsNull(user.getId());

        NotificationPageResponse pageResponse = new NotificationPageResponse(
                notificationDTOs, nextCursor, hasMore, unreadCount);
        return new ApiCustomResponse<>(pageResponse, "Notifications retrieved successfully", HttpStatus.OK.value());
    }

    // Mark notification as read
    @Transactional
    public void markAsRead(UUID notificationId, User user) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return;
        }

        Notification notification = notificationOpt.get();

        // Verify the notification belongs to the user
        if (!notification.getUser().getId().equals(user.getId())) {
            return;
        }

        // Skip if already read
        if (notification.getReadAt() != null) {
            return;
        }

        notification.setReadAt(Instant.now());
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);

        // Log read event
        logService.logNotificationEvent(notification, null, "INFO", "READ", "", null);
    }

    // Update notification status
    @Transactional
    public void updateStatus(UUID notificationId, NotificationStatus status) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setStatus(status);
            notificationRepository.save(notification);

            // Log status change
            logService.logNotificationEvent(notification, null, "INFO", "STATUS_UPDATED", "", null);
        }
    }

    // Update aggregation count for batched notifications
    @Transactional
    public void updateAggregationCount(UUID notificationId, int count) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setAggregationCount(count);
            notificationRepository.save(notification);

            // Log aggregation update
            logService.logNotificationEvent(notification, null, "INFO", "AGGREGATION_UPDATED", "", null);
        }
    }

    // Soft delete notification
    @Transactional
    public void deleteNotification(UUID notificationId, User user) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return;
        }

        Notification notification = notificationOpt.get();

        // Verify the notification belongs to the user
        if (!notification.getUser().getId().equals(user.getId())) {
            return;
        }

        notification.setDeletedAt(Instant.now());
        notificationRepository.save(notification);

        // Log deletion
        logService.logNotificationEvent(notification, null, "INFO", "DELETED", "", null);
    }

    // Map Notification to DTO
    private NotificationDTO mapToNotificationDTO(Notification notification) {
        return new NotificationDTO(
                notification.getTitle(),
                notification.getMessage(),
                notification.getReadAt() != null,
                notification.getCreatedAt());
    }
}
