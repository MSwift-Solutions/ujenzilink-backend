package com.ujenzilink.ujenzilink_backend.auth.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
    List<VerificationToken> findByUserId(UUID userId);

    int deleteByExpiresAtBefore(java.time.Instant expiryDate);
}
