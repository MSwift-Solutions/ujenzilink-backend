package com.ujenzilink.ujenzilink_backend.auth.models;

import com.ujenzilink.ujenzilink_backend.auth.enums.PasswordActionType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_tokens")
public class PasswordActionToken {

    @Id
    @Column(length = 6)
    private String token;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PasswordActionType actionType;

    public PasswordActionToken() {
    }

    public PasswordActionToken(String token, UUID userId, Instant expiresAt, PasswordActionType actionType) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.actionType = actionType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public PasswordActionType getActionType() {
        return actionType;
    }

    public void setActionType(PasswordActionType actionType) {
        this.actionType = actionType;
    }
}
