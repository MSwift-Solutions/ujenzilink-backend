package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.BackupFailureReportDTO;
import com.ujenzilink.ujenzilink_backend.notifications.services.EmailNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backup")
public class BackupReportController {

    private final EmailNotificationService emailNotificationService;

    @Value("${app.admin-api-key}")
    private String adminApiKey;

    @Value("${app.report-recipient}")
    private String reportRecipient;

    public BackupReportController(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping("/failure")
    public ResponseEntity<String> reportBackupFailure(
            @RequestHeader("x-api-key") String apiKey,
            @RequestBody BackupFailureReportDTO reportDTO) {

        if (adminApiKey == null || !adminApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API Key");
        }

        emailNotificationService.sendSystemFailureReportEmail(
                reportRecipient,
                reportDTO.hostname(),
                reportDTO.timestamp(),
                reportDTO.backup_file(),
                reportDTO.step(),
                reportDTO.error_message(),
                reportDTO.destination().host(),
                reportDTO.destination().dir()
        );

        return ResponseEntity.ok("Failure report received and email sent.");
    }
}
