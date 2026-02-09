package com.ujenzilink.ujenzilink_backend.notifications.services;

import com.ujenzilink.ujenzilink_backend.notifications.models.Notification;
import com.ujenzilink.ujenzilink_backend.notifications.models.NotificationDelivery;
import com.ujenzilink.ujenzilink_backend.notifications.models.NotificationLog;
import com.ujenzilink.ujenzilink_backend.notifications.repositories.NotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationLogService {

    @Autowired
    private NotificationLogRepository logRepository;

    // Log notification-related event
    @Transactional
    public void logNotificationEvent(
            Notification notification,
            NotificationDelivery delivery,
            String level,
            String action,
            String message,
            String metadata) {

        NotificationLog log = new NotificationLog();
        log.setNotification(notification);
        log.setDelivery(delivery);
        log.setLevel(level);
        log.setAction(action);
        log.setMessage(message);
        log.setMetadata(metadata);

        logRepository.save(log);
    }

    // Log delivery-related event
    @Transactional
    public void logDeliveryEvent(
            Notification notification,
            NotificationDelivery delivery,
            String level,
            String action,
            String message,
            String metadata) {

        logNotificationEvent(notification, delivery, level, action, message, metadata);
    }
}
