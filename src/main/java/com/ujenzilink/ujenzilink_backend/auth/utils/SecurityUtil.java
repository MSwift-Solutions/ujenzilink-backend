package com.ujenzilink.ujenzilink_backend.auth.utils;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtil {

    private final UserRepository userRepository;

    public SecurityUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }

            if (principal instanceof String username) {
                return username;
            }
        }

        return null;
    }

    public Optional<User> getAuthenticatedUser() {
        String email = getCurrentUsername();

        if (email == null) {
            return Optional.empty();
        }

        User user = userRepository.findFirstByEmail(email);
        return Optional.ofNullable(user);
    }
}
