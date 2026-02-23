package com.ujenzilink.ujenzilink_backend.auth.services;

import com.ujenzilink.ujenzilink_backend.auth.repositories.PasswordActionTokenRepository;
import com.ujenzilink.ujenzilink_backend.auth.repositories.VerificationTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TokenCleanupService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordActionTokenRepository passwordActionTokenRepository;

    public TokenCleanupService(VerificationTokenRepository verificationTokenRepository,
            PasswordActionTokenRepository passwordActionTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordActionTokenRepository = passwordActionTokenRepository;
    }

    /**
     * Cleans up expired tokens every hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();

        int deletedVerification = verificationTokenRepository.deleteByExpiresAtBefore(now);
        int deletedPassword = passwordActionTokenRepository.deleteByExpiresAtBefore(now);

        System.out.println("Cleaned up expired tokens: " + deletedVerification + " verification, " + deletedPassword
                + " password.");
    }
}
