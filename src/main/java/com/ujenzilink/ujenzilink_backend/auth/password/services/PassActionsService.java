package com.ujenzilink.ujenzilink_backend.auth.password.services;

import com.ujenzilink.ujenzilink_backend.auth.enums.PasswordActionType;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import com.ujenzilink.ujenzilink_backend.auth.models.PasswordActionToken;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.password.dto.CodeConfirmRequest;
import com.ujenzilink.ujenzilink_backend.auth.password.dto.ForgotPassResetNew;
import com.ujenzilink.ujenzilink_backend.auth.password.dto.ForgotPassResetRequest;
import com.ujenzilink.ujenzilink_backend.auth.password.dto.PassChangeRequest;
import com.ujenzilink.ujenzilink_backend.auth.password.model.PasswordAction;
import com.ujenzilink.ujenzilink_backend.auth.password.repository.PasswordActionRepository;
import com.ujenzilink.ujenzilink_backend.auth.repositories.PasswordActionTokenRepository;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.notifications.dtos.EmailNotificationDTO;
import com.ujenzilink.ujenzilink_backend.notifications.services.ResendNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PassActionsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordActionRepository passwordActionRepository;

    @Autowired
    private PasswordActionTokenRepository passwordActionTokenRepository;

    @Autowired
    private ResendNotificationService resendNotificationService;

    @Autowired
    private SecurityUtil securityUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ApiCustomResponse<String> changePassword(PassChangeRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            return new ApiCustomResponse<>(null, "Passwords do not match.", HttpStatus.BAD_REQUEST.value());
        }

        java.util.Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated or not found.", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        // Check if new password is same as old password
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            return new ApiCustomResponse<>(null, "New password cannot be the same as the current password.", HttpStatus.BAD_REQUEST.value());
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Record the action
        PasswordAction passwordAction = new PasswordAction();
        passwordAction.setActionType(PasswordActionType.PASSWORD_CHANGE);
        passwordAction.setActionDate(Instant.now());
        passwordAction.setInitiatedBy(Roles.ROLE_USER);
        passwordAction.setCompleted(true);
        passwordAction.setUser(user);
        passwordActionRepository.save(passwordAction);

        // Send email notification
        EmailNotificationDTO emailDetails = new EmailNotificationDTO(user.getEmail(), user.getFirstName(), null);
        resendNotificationService.sendPassChangeEmail(emailDetails, user);

        return new ApiCustomResponse<>(null, "Password changed successfully.", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> requestPasswordReset(ForgotPassResetRequest request) {
        User user = userRepository.findFirstByEmail(request.email().toLowerCase());

        if (user == null) {
            return new ApiCustomResponse<>(null, "Account not found, kindly register.", HttpStatus.OK.value());
        }

        if (!user.getIsEnabled()) {
            return new ApiCustomResponse<>(null, "Account not verified. Please verify your account first.", HttpStatus.FORBIDDEN.value());
        }

        // Invalidate any existing reset tokens for this user
        invalidateUserPasswordTokens(user.getId());

        // Generate reset code
        String resetCode = generatePasswordResetToken(user);

        // Send reset email
        EmailNotificationDTO emailDetails = new EmailNotificationDTO(user.getEmail(), user.getFirstName(), resetCode);
        resendNotificationService.sendPassResetEmail(emailDetails, user);

        // Create password action record (not completed yet)
        PasswordAction passwordAction = new PasswordAction();
        passwordAction.setActionType(PasswordActionType.PASSWORD_RESET);
        passwordAction.setActionDate(Instant.now());
        passwordAction.setInitiatedBy(Roles.ROLE_USER);
        passwordAction.setCompleted(false);
        passwordAction.setUser(user);
        passwordActionRepository.save(passwordAction);

        return new ApiCustomResponse<>(null, "Password reset code has been sent to " + user.getEmail(), HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> confirmResetCode(CodeConfirmRequest request) {
        // Retrieve and validate token
        PasswordActionToken tokenData = passwordActionTokenRepository.findById(request.resetCode()).orElse(null);

        if (tokenData == null) {
            return new ApiCustomResponse<>(null, "Invalid or expired reset code.", HttpStatus.BAD_REQUEST.value());
        }

        if (tokenData.getExpiresAt().isBefore(Instant.now())) {
            passwordActionTokenRepository.delete(tokenData);
            return new ApiCustomResponse<>(null, "Reset code has expired.", HttpStatus.GONE.value());
        }

        // Get user
        User user = userRepository.findById(tokenData.getUserId()).orElse(null);
        if (user == null) {
            return new ApiCustomResponse<>(null, "User not found, kindly register.", HttpStatus.NOT_FOUND.value());
        }

        // Mark the most recent password reset action as code confirmed
        PasswordAction passwordAction = passwordActionRepository.findTopByUserAndActionTypeOrderByActionDateDesc(user, PasswordActionType.PASSWORD_RESET);
        if (passwordAction != null) {
            passwordAction.setCodeConfirmed(true);
            passwordActionRepository.save(passwordAction);
        }

        return new ApiCustomResponse<>(null, "Reset code confirmed successfully.", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> resetPassword(ForgotPassResetNew request) {
        // Validate passwords match
        if (!request.newPassword().equals(request.confirmPassword())) {
            return new ApiCustomResponse<>(null, "Passwords do not match.", HttpStatus.BAD_REQUEST.value());
        }

        // Get user by email
        User user = userRepository.findFirstByEmail(request.email().toLowerCase());
        if (user == null) {
            return new ApiCustomResponse<>(null, "User not found, kindly register.", HttpStatus.NOT_FOUND.value());
        }

        // Check if code was confirmed
        PasswordAction passwordAction = passwordActionRepository.findTopByUserAndActionTypeOrderByActionDateDesc(user, PasswordActionType.PASSWORD_RESET);
        if (passwordAction == null || !passwordAction.getCodeConfirmed()) {
            return new ApiCustomResponse<>(null, "Reset code must be confirmed before resetting password.", HttpStatus.FORBIDDEN.value());
        }

        // Check if password action is still valid (not completed yet)
        if (passwordAction.getCompleted()) {
            return new ApiCustomResponse<>(null, "This reset code has already been used.", HttpStatus.BAD_REQUEST.value());
        }

        // Update password and reset account lock/failed attempts
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Mark the password reset action as completed
        passwordAction.setCompleted(true);
        passwordActionRepository.save(passwordAction);

        // Send confirmation email
        EmailNotificationDTO emailDetails = new EmailNotificationDTO(user.getEmail(), user.getFirstName(), null);
        resendNotificationService.sendPassChangeEmail(emailDetails, user);

        return new ApiCustomResponse<>(null, "Password reset successfully.", HttpStatus.OK.value());
    }

    private String generatePasswordResetToken(User user) {
        String resetCode = String.format("%06d", secureRandom.nextInt(1000000));
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        PasswordActionToken token = new PasswordActionToken(resetCode, user.getId(), expiresAt, PasswordActionType.PASSWORD_RESET);

        passwordActionTokenRepository.save(token);

        return resetCode;
    }

    private void invalidateUserPasswordTokens(UUID userId) {
        var tokens = passwordActionTokenRepository.findByUserId(userId);
        passwordActionTokenRepository.deleteAll(tokens);
    }
}
