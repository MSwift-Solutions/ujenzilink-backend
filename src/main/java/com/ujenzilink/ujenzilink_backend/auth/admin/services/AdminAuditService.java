package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminActionLog;
import com.ujenzilink.ujenzilink_backend.auth.admin.AdminUser;
import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import com.ujenzilink.ujenzilink_backend.auth.admin.repos.AdminActionLogRepository;
import com.ujenzilink.ujenzilink_backend.auth.admin.repos.AdminUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return "Unknown";
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
