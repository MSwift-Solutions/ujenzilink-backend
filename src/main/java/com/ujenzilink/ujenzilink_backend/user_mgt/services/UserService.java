package com.ujenzilink.ujenzilink_backend.user_mgt.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ApiCustomResponse<String> deleteUser() {
        String email = SecurityUtil.getCurrentUsername();

        if (email == null) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User user = userRepository.findFirstByEmail(email);

        if (user == null) {
            return new ApiCustomResponse<>(
                    null,
                    "User not found",
                    HttpStatus.NOT_FOUND.value());
        }

        user.setIsDeleted(true);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        return new ApiCustomResponse<>(
                "User deleted successfully",
                "User account has been deactivated",
                HttpStatus.OK.value());
    }
}
