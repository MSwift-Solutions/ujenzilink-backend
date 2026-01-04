package com.ujenzilink.ujenzilink_backend.auth.dtos;

import com.ujenzilink.ujenzilink_backend.auth.models.User;

import java.time.Instant;

public record TokenDetails(
                String token,
                Instant expiresAt,
                User user) {
}
