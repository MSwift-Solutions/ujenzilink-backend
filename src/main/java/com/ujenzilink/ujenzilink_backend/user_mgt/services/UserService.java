package com.ujenzilink.ujenzilink_backend.user_mgt.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserCountResponseDto;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserInfoDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public UserService(UserRepository userRepository, SecurityUtil securityUtil) {
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    public ApiCustomResponse<String> deleteUser() {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated or not found.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();
        user.setIsDeleted(true);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        return new ApiCustomResponse<>(
                "User deleted successfully",
                "User account has been deactivated",
                HttpStatus.OK.value());
    }

    public ApiCustomResponse<UserCountResponseDto> getUserCount() {
        long totalUsers = userRepository.count();

        List<User> allUsers = userRepository.findAll();
        Collections.shuffle(allUsers);

        List<UserInfoDto> randomUsers = new ArrayList<>();
        int userCount = Math.min(4, allUsers.size());

        for (int i = 0; i < userCount; i++) {
            User user = allUsers.get(i);
            String name = user.getFullName();
            String profileUrl = (user.getProfilePicture() != null) ? user.getProfilePicture().getUrl() : null;
            randomUsers.add(new UserInfoDto(name, profileUrl));
        }

        UserCountResponseDto responseDto = new UserCountResponseDto(totalUsers, randomUsers);

        return new ApiCustomResponse<>(
                responseDto,
                "User count retrieved successfully",
                HttpStatus.OK.value());
    }
}
