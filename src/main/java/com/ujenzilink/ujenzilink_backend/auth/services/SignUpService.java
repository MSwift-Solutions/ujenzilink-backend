package com.ujenzilink.ujenzilink_backend.auth.services;

import com.ujenzilink.ujenzilink_backend.auth.dtos.SignInResponse;
import com.ujenzilink.ujenzilink_backend.auth.dtos.SignUpRequest;
import com.ujenzilink.ujenzilink_backend.auth.dtos.EmailDetails;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.models.VerificationToken;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.repositories.VerificationTokenRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.JWTUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SignUpService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

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

        User existingUserByUsername = userRepository.findFirstByUsername(signUpRequest.username().toLowerCase());
        if (existingUserByUsername != null) {
            return new ApiCustomResponse<>(
                    null,
                    "Username already taken. Please choose a different username.",
                    HttpStatus.CONFLICT.value());
        }

        User user = new User();
        user.setFirstName(signUpRequest.firstName());
        user.setMiddleName(signUpRequest.middleName());
        user.setLastName(signUpRequest.lastName());
        user.setPhoneNumber(signUpRequest.phoneNumber());
        user.setEmail(signUpRequest.email().toLowerCase());
        user.setUsername(signUpRequest.username().toLowerCase());
        user.setPassword(new BCryptPasswordEncoder().encode(signUpRequest.password()));
        user.setRole(Roles.ROLE_USER);
        user.setHasAgreedToTerms(true);
        user.setTermsVersion("1.0");

        userRepository.save(user);

        String token = generateToken(user);

        EmailDetails emailDetails = new EmailDetails(
                signUpRequest.email(),
                signUpRequest.firstName(),
                token);
        emailService.sendConfirmationEmail(emailDetails, user);

        return new ApiCustomResponse<>(
                null,
                "Registration successful. Please check your email for the confirmation code.",
                HttpStatus.OK.value());
    }

    public String generateToken(User user) {
        // Use SecureRandom for cryptographically secure token generation
        String resetCode = String.format("%06d", secureRandom.nextInt(1000000));
        System.out.println("Registration code: " + resetCode);
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        VerificationToken verificationToken = new VerificationToken(resetCode, user.getId(), expiresAt);
        verificationTokenRepository.save(verificationToken);

        return resetCode;
    }

    @Transactional
    public ApiCustomResponse<SignInResponse> confirmToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findById(token).orElse(null);

        if (verificationToken == null) {
            return new ApiCustomResponse<>(
                    null,
                    "The verification token is invalid or has expired.",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            return new ApiCustomResponse<>(
                    null,
                    "The verification token has expired.",
                    HttpStatus.GONE.value());
        }

        // Token is valid, proceed with confirmation
        verificationTokenRepository.delete(verificationToken);

        User user = userRepository.findById(verificationToken.getUserId()).orElse(null);
        user.setIsEnabled(true);
        user.setConfirmedAt(Instant.now());
        userRepository.save(user);

        // Generate JWT token for immediate authentication
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwt = jwtUtil.generateToken(userDetails);

        EmailDetails emailDetails = new EmailDetails(
                user.getEmail(),
                user.getFirstName(),
                null);
        emailService.sendSuccessfulCreationEmail(emailDetails, user);

       SignInResponse confirmResponse = new SignInResponse(
                jwt,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUserHandle()
        );

        return new ApiCustomResponse<>(
                confirmResponse,
                "Account successfully verified. You can now upload your profile picture.",
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

        EmailDetails emailDetails = new EmailDetails(
                user.getEmail(),
                user.getFirstName(),
                token);
        emailService.sendConfirmationEmail(emailDetails, user);

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
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        if (user.getLastResendAttempt().isBefore(oneHourAgo)) {
            user.setResendVerificationCount(0);
            return false;
        }

        // Check if exceeded limit (3 attempts per hour)
        return user.getResendVerificationCount() >= 3;
    }

    // Helper method to update resend tracking
    private void updateResendTracking(User user) {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

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
        List<VerificationToken> userTokens = verificationTokenRepository.findByUserId(user.getId());
        verificationTokenRepository.deleteAll(userTokens);
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username.toLowerCase());
    }
}