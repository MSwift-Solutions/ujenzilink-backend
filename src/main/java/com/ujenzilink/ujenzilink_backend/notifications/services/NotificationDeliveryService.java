package com.ujenzilink.ujenzilink_backend.notifications.services;

import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationChannel;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationStatus;
import com.ujenzilink.ujenzilink_backend.notifications.models.Notification;
import com.ujenzilink.ujenzilink_backend.notifications.models.NotificationDelivery;
import com.ujenzilink.ujenzilink_backend.notifications.repositories.NotificationDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationDeliveryService {

    @Autowired
    private NotificationDeliveryRepository deliveryRepository;

    @Autowired
    private NotificationLogService logService;

    // Create a delivery record for a specific channel
    @Transactional
    public NotificationDelivery createDelivery(Notification notification, NotificationChannel channel) {
        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setNotification(notification);
        delivery.setChannel(channel);
        delivery.setStatus(NotificationStatus.PENDING);
        delivery.setRetryCount(0);
        delivery.setMaxRetries(3);

        NotificationDelivery saved = deliveryRepository.save(delivery);

        // Log delivery creation
        logService.logDeliveryEvent(notification, saved, "INFO", "DELIVERY_CREATED", "", null);

        return saved;
    }

    // Mark delivery as sent
    @Transactional
    public void markAsSent(UUID deliveryId) {
        Optional<NotificationDelivery> deliveryOpt = deliveryRepository.findById(deliveryId);
        if (deliveryOpt.isPresent()) {
            NotificationDelivery delivery = deliveryOpt.get();
            delivery.setStatus(NotificationStatus.SENT);
            delivery.setDeliveredAt(Instant.now());
            deliveryRepository.save(delivery);

            // Log successful delivery
            logService.logDeliveryEvent(delivery.getNotification(), delivery, "INFO", "SENT", "", null);
        }
    }

    // Mark delivery as failed and schedule retry if attempts remain
    @Transactional
    public void markAsFailed(UUID deliveryId, String failureReason) {
        Optional<NotificationDelivery> deliveryOpt = deliveryRepository.findById(deliveryId);
        if (deliveryOpt.isEmpty()) {
            return;
        }

        NotificationDelivery delivery = deliveryOpt.get();
        delivery.setLastAttemptAt(Instant.now());
        delivery.setRetryCount(delivery.getRetryCount() + 1);
        delivery.setFailureReason(failureReason);

        if (delivery.getRetryCount() >= delivery.getMaxRetries()) {
            // Max retries reached, mark as permanently failed
            delivery.setStatus(NotificationStatus.FAILED);
            delivery.setNextRetryAt(null);

            // Log permanent failure
            logService.logDeliveryEvent(delivery.getNotification(), delivery, "ERROR", "FAILED", failureReason, null);
        } else {
            // Schedule retry with exponential backoff
            long backoffMinutes = (long) Math.pow(2, delivery.getRetryCount()) * 5; // 5, 10, 20 minutes
            delivery.setNextRetryAt(Instant.now().plus(backoffMinutes, ChronoUnit.MINUTES));

            // Log retry scheduled
            logService.logDeliveryEvent(delivery.getNotification(), delivery, "WARN", "RETRY_SCHEDULED", failureReason,
                    null);
        }

        deliveryRepository.save(delivery);
    }

    // Record delivery attempt
    @Transactional
    public void recordAttempt(UUID deliveryId) {
        Optional<NotificationDelivery> deliveryOpt = deliveryRepository.findById(deliveryId);
        if (deliveryOpt.isPresent()) {
            NotificationDelivery delivery = deliveryOpt.get();
            delivery.setLastAttemptAt(Instant.now());
            deliveryRepository.save(delivery);

            // Log attempt
            logService.logDeliveryEvent(delivery.getNotification(), delivery, "INFO", "ATTEMPT", "", null);
        }
    }
}
