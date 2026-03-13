package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminLoginHistory;
import com.ujenzilink.ujenzilink_backend.auth.admin.AdminUser;
import com.ujenzilink.ujenzilink_backend.auth.admin.repos.AdminLoginHistoryRepository;
import com.ujenzilink.ujenzilink_backend.auth.admin.repos.AdminUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdminAuthService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;
    private final AdminLoginHistoryRepository adminLoginHistoryRepository;

    public AdminAuthService(AdminUserRepository adminUserRepository,
                            AdminLoginHistoryRepository adminLoginHistoryRepository) {
        this.adminUserRepository = adminUserRepository;
        this.adminLoginHistoryRepository = adminLoginHistoryRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return adminUserRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Admin account not found."));
    }

    @Transactional
    public void recordLoginSuccess(String email, HttpServletRequest request) {
        adminUserRepository.findByEmail(email.toLowerCase()).ifPresent(admin -> {
            admin.setLastLoginAt(Instant.now());
            adminUserRepository.save(admin);

            AdminLoginHistory history = new AdminLoginHistory();
            history.setAdminUser(admin);
            history.setSuccess(true);
            history.setIpAddress(resolveClientIp(request));
            history.setUserAgent(request.getHeader("User-Agent"));
            adminLoginHistoryRepository.save(history);
        });
    }

    @Transactional
    public void recordLoginFailure(String email, String reason, HttpServletRequest request) {
        adminUserRepository.findByEmail(email.toLowerCase()).ifPresent(admin -> {
            AdminLoginHistory history = new AdminLoginHistory();
            history.setAdminUser(admin);
            history.setSuccess(false);
            history.setFailureReason(reason);
            history.setIpAddress(resolveClientIp(request));
            history.setUserAgent(request.getHeader("User-Agent"));
            adminLoginHistoryRepository.save(history);
        });
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
