package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UserDeletionRequestResponse;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.notifications.services.ResendNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final ResendNotificationService resendNotificationService;

    public AdminUserManagementService(UserRepository userRepository, ResendNotificationService resendNotificationService) {
        this.userRepository = userRepository;
        this.resendNotificationService = resendNotificationService;
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

    public ApiCustomResponse<String> revertUserDeletion(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not found.",
                    HttpStatus.NOT_FOUND.value()
            );
        }

        User user = userOpt.get();

        if (!user.getIsDeleted()) {
            return new ApiCustomResponse<>(
                    null,
                    "User account is not deleted.",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        user.setIsDeleted(false);
        user.setDeletedAt(null);
        userRepository.save(user);

        // Send revert notification email
        resendNotificationService.sendAccountDeletionRevertEmail(user.getEmail(), user.getFirstName(), user);

        return new ApiCustomResponse<>(
                "User deletion reverted successfully",
                "User account has been restored",
                HttpStatus.OK.value()
        );
    }
}
