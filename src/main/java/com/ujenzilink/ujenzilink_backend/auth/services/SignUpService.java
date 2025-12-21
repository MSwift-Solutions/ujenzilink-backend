package com.ujenzilink.ujenzilink_backend.auth.services;

import com.ujenzilink.ujenzilink_backend.auth.dtos.SignUpRequest;
import com.ujenzilink.ujenzilink_backend.auth.dtos.TokenDetails;
import com.ujenzilink.ujenzilink_backend.auth.dtos.EmailDetails;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SignUpService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    private final Map<String, TokenDetails> tokenStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional(rollbackFor = Exception.class)
    public ApiCustomResponse<String> createUser(SignUpRequest signUpRequest, boolean agree) {
        if (!agree) {
            return new ApiCustomResponse<>(
                    null,
                    "Terms acceptance required to sign up.",
                    HttpStatus.BAD_REQUEST.value());
        }

        User existingUser = userRepository.findFirstByEmail(signUpRequest.email().toLowerCase());
        if (existingUser != null) {
            return new ApiCustomResponse<>(
                    null,
                    "Email already registered. Please log in.",
                    HttpStatus.CONFLICT.value());
        }

        User user = new User();
        user.setFirstName(signUpRequest.firstName());
        user.setMiddleName(signUpRequest.middleName());
        user.setLastName(signUpRequest.lastName());
        user.setPhoneNumber(signUpRequest.phoneNumber());
        user.setEmail(signUpRequest.email().toLowerCase());
        user.setDateOfCreation(LocalDateTime.now());
        user.setPassword(new BCryptPasswordEncoder().encode(signUpRequest.password()));
        user.setRole(Roles.ROLE_USER);
        user.setHasAgreedToTerms(true);
        user.setTermsAgreedAt(LocalDateTime.now());
        user.setTermsVersion("1.0");

        userRepository.save(user);

        String token = generateToken(user);
        System.out.println("Confirmation token: " + token);

        EmailDetails emailDetails = new EmailDetails(
                signUpRequest.email(),
                signUpRequest.firstName(),
                token);
        emailService.sendConfirmationEmail(emailDetails);

        return new ApiCustomResponse<>(
                null,
                "Registration successful. Please check your email for the confirmation code.",
                HttpStatus.OK.value());
    }

    public String generateToken(User user) {
        // Use SecureRandom for cryptographically secure token generation
        String resetCode = String.format("%06d", secureRandom.nextInt(1000000));
        System.out.println("Registration code: " + resetCode);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        TokenDetails tokenDetails = new TokenDetails(resetCode, expiresAt, user);
        tokenStore.put(resetCode, tokenDetails);

        scheduleTokenRemoval(resetCode, expiresAt);

        return resetCode;
    }

    private void scheduleTokenRemoval(String token, LocalDateTime expiresAt) {
        long delay = java.time.Duration.between(LocalDateTime.now(), expiresAt).toMillis();
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                tokenStore.remove(token);
            }
        }, delay);
    }

    @Transactional
    public ApiCustomResponse<String> confirmToken(String token) {
        TokenDetails tokenDetails = tokenStore.get(token);

        if (tokenDetails == null) {
            return new ApiCustomResponse<>(
                    null,
                    "The verification token is invalid or has expired.",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (tokenDetails.expiresAt().isBefore(LocalDateTime.now())) {
            tokenStore.remove(token); // Remove expired token
            return new ApiCustomResponse<>(
                    null,
                    "The verification token has expired.",
                    HttpStatus.GONE.value());
        }

        // Token is valid, you can proceed with confirmation
        tokenStore.remove(token); // Remove the token after successful confirmation

        User user = tokenDetails.user();
        user.setIsEnabled(true);
        user.setConfirmedAt(LocalDateTime.now());
        userRepository.save(user);

        EmailDetails emailDetails = new EmailDetails(
                user.getEmail(),
                user.getFirstName(),
                null);
        emailService.sendSuccessfulCreationEmail(emailDetails);

        return new ApiCustomResponse<>(
                null,
                "Account successfully verified. You may now log in.",
                HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> resendVerification(String email) {
        User user = userRepository.findFirstByEmail(email.toLowerCase());

        if (user == null) {
            return new ApiCustomResponse<>(
                    null,
                    "Email not found, kindly register.",
                    HttpStatus.OK.value());
        }

        if (user.getIsEnabled()) {
            return new ApiCustomResponse<>(
                    null,
                    "Account already verified. Please log in.",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (hasExceededResendLimit(user)) {
            return new ApiCustomResponse<>(
                    null,
                    "Too many verification requests. Please try again later.",
                    HttpStatus.TOO_MANY_REQUESTS.value());
        }

        invalidateUserTokens(user);

        String token = generateToken(user);
        System.out.println("Resent confirmation token: " + token);

        EmailDetails emailDetails = new EmailDetails(
                user.getEmail(),
                user.getFirstName(),
                token);
        emailService.sendConfirmationEmail(emailDetails);

        updateResendTracking(user);

        return new ApiCustomResponse<>(
                null,
                "If this email is registered and unverified, a new verification code has been sent.",
                HttpStatus.OK.value());
    }

    private boolean hasExceededResendLimit(User user) {
        if (user.getLastResendAttempt() == null) {
            return false;
        }

        // Reset counter if more than 1 hour has passed
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        if (user.getLastResendAttempt().isBefore(oneHourAgo)) {
            user.setResendVerificationCount(0);
            return false;
        }

        // Check if exceeded limit (3 attempts per hour)
        return user.getResendVerificationCount() >= 3;
    }

    // Helper method to update resend tracking
    private void updateResendTracking(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        // Reset count if last attempt was more than an hour ago
        if (user.getLastResendAttempt() == null ||
                user.getLastResendAttempt().isBefore(oneHourAgo)) {
            user.setResendVerificationCount(1);
        } else {
            user.setResendVerificationCount(user.getResendVerificationCount() + 1);
        }

        user.setLastResendAttempt(now);
        userRepository.save(user);
    }

    // Helper method to invalidate all tokens for a user
    private void invalidateUserTokens(User user) {
        // Remove all tokens for this user from the token store
        tokenStore.entrySet().removeIf(entry -> entry.getValue().user().getId().equals(user.getId()));
    }
}