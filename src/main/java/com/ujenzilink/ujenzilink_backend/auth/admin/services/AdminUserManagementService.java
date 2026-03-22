package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UserDeletionRequestResponse;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementService {

    private final UserRepository userRepository;

    public AdminUserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ApiCustomResponse<List<UserDeletionRequestResponse>> getUsersWithDeletionRequests() {
        List<User> deletedUsers = userRepository.findByIsDeletedTrueOrderByDeletedAtDesc();

        List<UserDeletionRequestResponse> responseDtos = deletedUsers.stream()
                .map(user -> new UserDeletionRequestResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getUserHandle(),
                        user.getDeletedAt(),
                        user.getProfilePicture() != null ? user.getProfilePicture().getUrl() : null
                ))
                .collect(Collectors.toList());

        return new ApiCustomResponse<>(
                responseDtos,
                "Users with deletion requests retrieved successfully",
                HttpStatus.OK.value()
        );
    }
}
