package com.ujenzilink.ujenzilink_backend.notifications.controllers;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.notifications.dtos.NotificationPageResponse;
import com.ujenzilink.ujenzilink_backend.notifications.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SecurityUtil securityUtil;

    // Get notifications with cursor pagination (automatically marks fetched
    // notifications as read)
    @GetMapping
    public ResponseEntity<ApiCustomResponse<NotificationPageResponse>> getNotifications(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value()));
        }

        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(new ApiCustomResponse<>(null, "Size must be between 1 and 100", 400));
        }

        ApiCustomResponse<NotificationPageResponse> response = notificationService.getNotifications(
                userOpt.get(), cursor, size);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    // Delete notification
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiCustomResponse<String>> deleteNotification(@PathVariable UUID id) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value()));
        }

        notificationService.deleteNotification(id, userOpt.get());
        return ResponseEntity.ok(new ApiCustomResponse<>("Notification deleted",
                "Notification deleted successfully", HttpStatus.OK.value()));
    }
}
