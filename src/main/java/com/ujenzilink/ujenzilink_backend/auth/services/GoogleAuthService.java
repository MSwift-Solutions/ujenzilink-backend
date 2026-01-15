package com.ujenzilink.ujenzilink_backend.auth.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.ujenzilink.ujenzilink_backend.auth.dtos.SignInResponse;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import com.ujenzilink.ujenzilink_backend.auth.enums.SignupMethod;
import com.ujenzilink.ujenzilink_backend.auth.enums.VerificationStatus;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.JWTUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class GoogleAuthService {

    @Autowired
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ApiCustomResponse<SignInResponse> authenticateWithGoogle(String idToken) {
        try {
            GoogleIdToken.Payload payload = googleTokenVerifierService.verifyToken(idToken);

            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String fullName = (String) payload.get("name");

            if (firstName == null && fullName != null) {
                String[] nameParts = fullName.split(" ", 2);
                firstName = nameParts[0];
                lastName = nameParts.length > 1 ? nameParts[1] : "";
            }

            User user = userRepository.findFirstByEmail(email.toLowerCase());

            if (user == null) {
                user = createGoogleUser(email, firstName, lastName);
            } else {
                if (user.getIsDeleted()) {
                    return new ApiCustomResponse<>(
                            null,
                            "This account has been deleted. Please contact support.",
                            HttpStatus.FORBIDDEN.value());
                }

                user.setLastSuccessfulLogin(Instant.now());
                userRepository.save(user);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtUtil.generateToken(userDetails);

            SignInResponse response = new SignInResponse(
                    jwt,
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getUserHandle());

            return new ApiCustomResponse<>(
                    response,
                    "Successfully authenticated with Google",
                    HttpStatus.OK.value());

        } catch (GeneralSecurityException | IOException e) {
            return new ApiCustomResponse<>(
                    null,
                    "Invalid Google ID token: " + e.getMessage(),
                    HttpStatus.UNAUTHORIZED.value());
        } catch (IllegalArgumentException e) {
            return new ApiCustomResponse<>(
                    null,
                    e.getMessage(),
                    HttpStatus.UNAUTHORIZED.value());
        } catch (Exception e) {
            return new ApiCustomResponse<>(
                    null,
                    "Authentication failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private User createGoogleUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email.toLowerCase());
        user.setFirstName(firstName != null ? firstName : "");
        user.setLastName(lastName != null ? lastName : "");

        String username = generateUsernameFromEmail(email);
        user.setUsername(username);

        String randomPassword = generateSecureRandomPassword();
        user.setPassword(new BCryptPasswordEncoder().encode(randomPassword));

        user.setRole(Roles.ROLE_USER);
        user.setSignupMethod(SignupMethod.GOOGLE);
        user.setIsEnabled(true);
        user.setConfirmedAt(Instant.now());
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        user.setHasAgreedToTerms(true);
        user.setTermsVersion("1.0");

        return userRepository.save(user);
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.split("@")[0].toLowerCase();
        baseUsername = baseUsername.replaceAll("[^a-z0-9._]", "");

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    private String generateSecureRandomPassword() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        StringBuilder password = new StringBuilder();
        for (byte b : randomBytes) {
            password.append(String.format("%02x", b));
        }
        return password.toString();
    }
}
