package com.ujenzilink.ujenzilink_backend.auth.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.PasswordActionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordActionTokenRepository extends JpaRepository<PasswordActionToken, String> {
    List<PasswordActionToken> findByUserId(UUID userId);

    int deleteByExpiresAtBefore(java.time.Instant expiryDate);
}
