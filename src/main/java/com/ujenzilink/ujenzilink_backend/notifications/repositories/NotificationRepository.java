package com.ujenzilink.ujenzilink_backend.notifications.repositories;

import com.ujenzilink.ujenzilink_backend.notifications.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
