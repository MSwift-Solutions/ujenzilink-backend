package com.ujenzilink.ujenzilink_backend.auth.models;

import com.ujenzilink.ujenzilink_backend.auth.enums.PasswordActionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;

@RedisHash(value = "password_tokens", timeToLive = 900)
public class PasswordActionToken {

    @Id
    private String token;

    @Indexed
    private Long userId;

    private Instant expiresAt;

    private PasswordActionType actionType;

    public PasswordActionToken() {
    }

    public PasswordActionToken(String token, Long userId, Instant expiresAt, PasswordActionType actionType) {
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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
