package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminMetricsResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UserDeletionRequestResponse;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.notifications.services.ResendNotificationService;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.user_mgt.repositories.UserActivityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;
    private final UserActivityRepository userActivityRepository;
    private final ResendNotificationService resendNotificationService;

    public AdminUserManagementService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            PostRepository postRepository,
            UserActivityRepository userActivityRepository,
            ResendNotificationService resendNotificationService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.postRepository = postRepository;
        this.userActivityRepository = userActivityRepository;
        this.resendNotificationService = resendNotificationService;
    }

    public ApiCustomResponse<AdminMetricsResponse> getAdminMetrics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsDeletedFalse();
        long deletedUsers = userRepository.countByIsDeletedTrue();
        long totalProjects = projectRepository.countByIsDeletedFalse();
        long totalPosts = postRepository.countByIsDeletedFalse();
        long activeUsersToday = userActivityRepository.countActiveUsersByDate(LocalDate.now());

        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long joinedToday = userRepository.countByDateOfCreationAfter(todayStart);

        Instant weekStart = Instant.now().minus(7, ChronoUnit.DAYS);
        long joinedThisWeek = userRepository.countByDateOfCreationAfter(weekStart);

        AdminMetricsResponse metrics = new AdminMetricsResponse(
                totalUsers,
                activeUsers,
                deletedUsers,
                totalProjects,
                totalPosts,
                activeUsersToday,
                joinedToday,
                joinedThisWeek
        );

        return new ApiCustomResponse<>(
                metrics,
                "Admin metrics retrieved successfully",
                HttpStatus.OK.value()
        );
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
