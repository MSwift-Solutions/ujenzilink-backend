package com.ujenzilink.ujenzilink_backend.notifications.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationStatus;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.models.Notification;
import com.ujenzilink.ujenzilink_backend.notifications.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
}
