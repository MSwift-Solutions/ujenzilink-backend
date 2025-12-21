package com.ujenzilink.ujenzilink_backend.auth.dtos;

import com.ujenzilink.ujenzilink_backend.auth.models.User;

import java.time.LocalDateTime;

public record TokenDetails(
        String token,
        LocalDateTime expiresAt,
        User user
) {}
