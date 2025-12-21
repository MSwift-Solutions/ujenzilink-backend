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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SignUpService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    private final Map<String, TokenDetails> tokenStore = new ConcurrentHashMap<>();

    public ApiCustomResponse<String> createUser(SignUpRequest signUpRequest, boolean agree) {
        if (!agree) {
            return new ApiCustomResponse<>(
                    null,
                    "You must agree to the terms and conditions to sign up.",
                    HttpStatus.BAD_REQUEST.value());
        }

        User existingUser = userRepository.findFirstByEmail(signUpRequest.email().toLowerCase());
        if (existingUser != null) {
            return new ApiCustomResponse<>(
                    null,
                    "User with " + signUpRequest.email().toLowerCase() + " already exists. Kindly proceed to login!",
                    HttpStatus.CONFLICT.value());
        }

        User user = new User();
        user.setFirstName(signUpRequest.firstName());
        user.setLastName(signUpRequest.lastName());
        user.setPhoneNumber(signUpRequest.phoneNumber());
        user.setEmail(signUpRequest.email().toLowerCase());
        user.setDateOfCreation(LocalDateTime.now());
        user.setPassword(new BCryptPasswordEncoder().encode(signUpRequest.password()));
        user.setRole(Roles.ROLE_USER);
        user.setHasAgreedToTerms(true);
        user.setTermsAgreedAt(LocalDateTime.now());
        user.setTermsVersion("1.0");

        User createdUser = userRepository.save(user);

        String token = generateToken(user);
        System.out.println("Confirmation token: " + token);

        EmailDetails emailDetails = new EmailDetails(
                signUpRequest.email(),
                signUpRequest.firstName(),
                token);
        emailService.sendConfirmationEmail(emailDetails);

        return new ApiCustomResponse<>(
                null,
                "Dear " + createdUser.getFirstName() + " ,your registration has been received. Kindly check your email("
                        + createdUser.getEmail() + ") and confirm your account.",
                HttpStatus.OK.value());
    }

    public String generateToken(User user) {
        Random random = new Random();
        String resetCode = String.valueOf(random.nextInt(900000) + 100000);
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

    // Method to confirm the token
    public ApiCustomResponse<String> confirmToken(String token) {
        TokenDetails tokenDetails = tokenStore.get(token);

        if (tokenDetails == null) {
            return new ApiCustomResponse<>(
                    null,
                    "Token not found or expired",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (tokenDetails.expiresAt().isBefore(LocalDateTime.now())) {
            tokenStore.remove(token); // Remove expired token
            return new ApiCustomResponse<>(
                    null,
                    "Token is expired",
                    HttpStatus.NOT_ACCEPTABLE.value());
        }

        // Token is valid, you can proceed with confirmation
        tokenStore.remove(token); // Remove the token after successful confirmation

        User user = tokenDetails.user();
        user.setIsEnabled(true);
        userRepository.save(user);

        EmailDetails emailDetails = new EmailDetails(
                user.getEmail(),
                user.getFirstName(),
                null);
        emailService.sendSuccessfulCreationEmail(emailDetails);

        return new ApiCustomResponse<>(
                null,
                "Token is valid, you can proceed to login",
                HttpStatus.OK.value());
    }
}