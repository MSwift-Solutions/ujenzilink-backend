package com.ujenzilink.ujenzilink_backend.auth.admin;

import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_action_logs")
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = true) // Nullable for system-level actions if needed
    private AdminUser adminUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AdminActionType action;

    @Column(updatable = false, length = 100)
    private String resourceId; // The ID of the affected resource (e.g. User ID, Document ID)

    @Column(columnDefinition = "TEXT", updatable = false)
    private String actionDetails; // Additional metadata or description

    @Column(updatable = false, length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public AdminActionLog() {}

    public AdminActionLog(AdminUser adminUser, AdminActionType action, String resourceId, String actionDetails, String ipAddress) {
        this.adminUser = adminUser;
        this.action = action;
        this.resourceId = resourceId;
        this.actionDetails = actionDetails;
        this.ipAddress = ipAddress;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AdminUser getAdminUser() { return adminUser; }
    public void setAdminUser(AdminUser adminUser) { this.adminUser = adminUser; }

    public AdminActionType getAction() { return action; }
    public void setAction(AdminActionType action) { this.action = action; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getActionDetails() { return actionDetails; }
    public void setActionDetails(String actionDetails) { this.actionDetails = actionDetails; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
