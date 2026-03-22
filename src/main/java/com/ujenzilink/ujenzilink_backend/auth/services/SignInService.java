package com.ujenzilink.ujenzilink_backend.auth.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.services.NotificationService;
import com.ujenzilink.ujenzilink_backend.notifications.services.ResendNotificationService;
import com.ujenzilink.ujenzilink_backend.user_mgt.enums.ActivityType;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.ActivityService;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Primary
@Service
public class SignInService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final NotificationService notificationService;
    private final ResendNotificationService resendNotificationService;

    public SignInService(UserRepository userRepository,
                         ActivityService activityService,
                         NotificationService notificationService,
                         ResendNotificationService resendNotificationService) {
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.notificationService = notificationService;
        this.resendNotificationService = resendNotificationService;
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findFirstByEmail(email.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException("Account not found. Please register.");
        }

        if (user.getIsDeleted()) {
            throw new UsernameNotFoundException("Account is deleted. Please register again.");
        }

        // Return Spring Security User with correct status flags
        // User(username, password, enabled, accountNonExpired, credentialsNonExpired,
        // accountNonLocked, authorities)
        return user;
    }

    public User findUserByEmail(String email) {
        return userRepository.findFirstByEmail(email.toLowerCase());
    }

    // Track login attempt (both successful and failed)
    @Transactional
    public void trackLoginAttempt(String email) {
        User user = userRepository.findFirstByEmail(email.toLowerCase());
        if (user != null) {
            user.setLastLoginAttempt(Instant.now());
            userRepository.save(user);
        }
    }

    // Track successful login
    @Transactional
    public void trackSuccessfulLogin(String email) {
        User user = userRepository.findFirstByEmail(email.toLowerCase());
        if (user != null) {
            user.setLastSuccessfulLogin(Instant.now());
            user.setFailedLoginAttempts(0);
            user.setIsLocked(false);
            userRepository.save(user);

            // Log login activity
            activityService.logActivity(user, ActivityType.LOGIN, null);

            // Create sign-in notification
            notificationService.createNotification(
                    user,
                    null,
                    NotificationType.SIGNIN_SUCCESS,
                    "New Sign-in",
                    "You signed in successfully.",
                    NotificationPriority.MEDIUM,
                    false,
                    null,
                    null);
        }
    }

    // Track failed login attempt and lock account after 3 attempts
    @Transactional
    public void trackFailedLoginAttempt(String email) {
        User user = userRepository.findFirstByEmail(email.toLowerCase());
        if (user != null) {
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= 3) {
                user.setIsLocked(true);

                // Send critical email
                resendNotificationService.sendAccountLockedEmail(user.getEmail(), user.getFirstName(), user);

                // Create in-app notification
                notificationService.createNotification(
                        user,
                        null,
                        NotificationType.ACCOUNT_SECURITY,
                        "Account Locked",
                        "Your account has been locked due to too many failed login attempts.",
                        NotificationPriority.URGENT,
                        false,
                        null,
                        null);
            }

            userRepository.save(user);
        }
    }
}
