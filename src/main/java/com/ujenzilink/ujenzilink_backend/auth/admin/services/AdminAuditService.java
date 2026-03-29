package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminActionLog;
import com.ujenzilink.ujenzilink_backend.auth.admin.AdminUser;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminActionLogPageResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminActionLogResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import com.ujenzilink.ujenzilink_backend.auth.admin.repos.AdminActionLogRepository;
import com.ujenzilink.ujenzilink_backend.auth.admin.repos.AdminUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AdminAuditService {

    private final AdminActionLogRepository adminActionLogRepository;
    private final AdminUserRepository adminUserRepository;

    public AdminAuditService(AdminActionLogRepository adminActionLogRepository, 
                             AdminUserRepository adminUserRepository) {
        this.adminActionLogRepository = adminActionLogRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Transactional
    public void logAction(AdminActionType action, String resourceId, String details, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AdminUser admin = null;

        if (auth != null && auth.getPrincipal() instanceof AdminUser) {
            admin = (AdminUser) auth.getPrincipal();
        } else if (auth != null && auth.getName() != null) {
            // Backup for cases where principal is not directly an AdminUser object
            admin = adminUserRepository.findByEmail(auth.getName()).orElse(null);
        }

        AdminActionLog log = new AdminActionLog(
            admin,
            action,
            resourceId,
            details,
            resolveClientIp(request)
        );

        adminActionLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public AdminActionLogPageResponse getAuditLogs(String cursor, Integer size) {
        if (size == null || size < 1) size = 50;
        if (size > 100) size = 100;

        Instant cursorTime = null;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decodedJson = new String(Base64.getDecoder().decode(cursor));
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> cursorData = mapper.readValue(decodedJson, Map.class);
                String timestamp = (String) cursorData.get("timestamp");
                cursorTime = Instant.parse(timestamp);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid cursor format");
            }
        }

        Pageable pageable = PageRequest.of(0, size + 1);
        List<AdminActionLog> logs;

        if (cursorTime != null) {
            logs = adminActionLogRepository.findByCreatedAtBeforeWithAdmin(cursorTime, pageable);
        } else {
            logs = adminActionLogRepository.findAllWithAdmin(pageable);
        }

        boolean hasMore = logs.size() > size;
        if (hasMore) {
            logs = logs.subList(0, size);
        }

        List<AdminActionLogResponse> responseLogs = logs.stream()
            .map(log -> new AdminActionLogResponse(
                log.getId(),
                log.getAdminUser() != null ? log.getAdminUser().getEmail() : "SYSTEM",
                log.getAdminUser() != null ? log.getAdminUser().getName() : "SYSTEM",
                log.getAction(),
                log.getResourceId(),
                log.getActionDetails(),
                log.getIpAddress(),
                log.getCreatedAt()
            )).toList();

        String nextCursor = null;
        if (hasMore && !logs.isEmpty()) {
            try {
                AdminActionLog lastLog = logs.get(logs.size() - 1);
                String cursorJson = String.format("{\"timestamp\":\"%s\"}", lastLog.getCreatedAt().toString());
                nextCursor = Base64.getEncoder().encodeToString(cursorJson.getBytes());
            } catch (Exception e) {
            }
        }

        return new AdminActionLogPageResponse(
            responseLogs,
            nextCursor,
            hasMore
        );
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return "Unknown";
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
