package com.ujenzilink.ujenzilink_backend.auth.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SignInService implements UserDetailsService {
    private final UserRepository userRepository;

    public SignInService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findFirstByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Account not found. Please register.");
        }

        if (user.getIsDeleted()) {
            throw new UsernameNotFoundException("Account is deleted. Please register again.");
        }

        // Return Spring Security User with correct status flags
        // User(username, password, enabled, accountNonExpired, credentialsNonExpired,
        // accountNonLocked, authorities)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.getIsLocked(), // accountNonLocked
                user.getAuthorities());
    }

    public User findUserByEmail(String email) {
        return userRepository.findFirstByEmail(email);
    }

    // Track login attempt (both successful and failed)
    @Transactional
    public void trackLoginAttempt(String email) {
        User user = userRepository.findFirstByEmail(email);
        if (user != null) {
            user.setLastLoginAttempt(Instant.now());
            userRepository.save(user);
        }
    }

    // Track successful login
    @Transactional
    public void trackSuccessfulLogin(String email) {
        User user = userRepository.findFirstByEmail(email);
        if (user != null) {
            user.setLastSuccessfulLogin(Instant.now());
            user.setFailedLoginAttempts(0);
            user.setIsLocked(false);
            userRepository.save(user);
        }
    }

    // Track failed login attempt and lock account after 3 attempts
    @Transactional
    public void trackFailedLoginAttempt(String email) {
        User user = userRepository.findFirstByEmail(email);
        if (user != null) {
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= 3) {
                user.setIsLocked(true);
            }

            userRepository.save(user);
        }
    }
}
