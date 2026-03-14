package com.ujenzilink.ujenzilink_backend.auth.admin;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_login_history")
public class AdminLoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false, updatable = false)
    private AdminUser adminUser;

    @Column(nullable = false, updatable = false)
    private boolean success;

    @Column(updatable = false, length = 45)
    private String ipAddress;

    @Column(updatable = false, length = 512)
    private String userAgent;

    @Column(updatable = false, length = 255)
    private String failureReason;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant loginAt;

    public AdminLoginHistory() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AdminUser getAdminUser() { return adminUser; }
    public void setAdminUser(AdminUser adminUser) { this.adminUser = adminUser; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Instant getLoginAt() { return loginAt; }
    public void setLoginAt(Instant loginAt) { this.loginAt = loginAt; }
}
