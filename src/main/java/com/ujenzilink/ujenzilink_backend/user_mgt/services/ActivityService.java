package com.ujenzilink.ujenzilink_backend.user_mgt.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.ActivityBreakdown;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserActivityStatsResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.enums.ActivityType;
import com.ujenzilink.ujenzilink_backend.user_mgt.models.UserActivity;
import com.ujenzilink.ujenzilink_backend.user_mgt.repositories.UserActivityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final UserActivityRepository userActivityRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public ActivityService(UserActivityRepository userActivityRepository,
            UserRepository userRepository,
            SecurityUtil securityUtil) {
        this.userActivityRepository = userActivityRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    /**
     * Log a user activity asynchronously to avoid blocking the main thread
     */
    @Async
    @Transactional
    public void logActivity(User user, ActivityType activityType, UUID entityId) {
        if (user == null || activityType == null) {
            return;
        }

        LocalDate activityDate = LocalDate.now(ZoneId.systemDefault());
        UserActivity activity = new UserActivity(user, activityType, entityId, activityDate);
        userActivityRepository.save(activity);
    }

    /**
     * Get activity stats for the authenticated user
     */
    public ApiCustomResponse<UserActivityStatsResponse> getMyActivityStats() {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated or not found.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();
        return getUserActivityStats(user);
    }

    /**
     * Get activity stats for a specific user by username
     */
    public ApiCustomResponse<UserActivityStatsResponse> getUserActivityStats(String username) {
        User user = userRepository.findFirstByUsername(username);

        if (user == null || user.getIsDeleted()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not found.",
                    HttpStatus.NOT_FOUND.value());
        }

        return getUserActivityStats(user);
    }

    /**
     * Build activity stats for a user
     */
    private ApiCustomResponse<UserActivityStatsResponse> getUserActivityStats(User user) {
        // Get total activities count
        long totalActivities = userActivityRepository.countByUser_Id(user.getId());

        // Get all distinct activity dates for streak calculation
        List<LocalDate> activityDates = userActivityRepository.findDistinctActivityDatesByUserId(user.getId());
        long activeDays = activityDates.size();

        // Calculate max streak
        long maxStreak = calculateMaxStreak(activityDates);

        // Build activity breakdown
        ActivityBreakdown breakdown = buildBreakdown(user.getId());

        // Build daily stats (date -> count map)
        Map<String, Integer> dailyStats = buildDailyStats(user.getId());

        UserActivityStatsResponse stats = new UserActivityStatsResponse(
                totalActivities,
                activeDays,
                maxStreak,
                breakdown,
                dailyStats);

        return new ApiCustomResponse<>(
                stats,
                "Activity stats retrieved successfully",
                HttpStatus.OK.value());
    }

    /**
     * Calculate the longest streak of consecutive days with activity
     */
    private long calculateMaxStreak(List<LocalDate> activityDates) {
        if (activityDates == null || activityDates.isEmpty()) {
            return 0;
        }

        long maxStreak = 1;
        long currentStreak = 1;

        for (int i = 1; i < activityDates.size(); i++) {
            LocalDate previousDate = activityDates.get(i - 1);
            LocalDate currentDate = activityDates.get(i);

            // Check if dates are consecutive
            if (previousDate.plusDays(1).equals(currentDate)) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return maxStreak;
    }

    /**
     * Build daily stats as a map of date string to activity count
     */
    private Map<String, Integer> buildDailyStats(UUID userId) {
        List<UserActivity> activities = userActivityRepository.findByUser_Id(userId);

        // Group activities by date and count them
        Map<String, Integer> dailyStats = activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getActivityDate().toString(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        return dailyStats;
    }

    /**
     * Build activity breakdown by category
     */
    private ActivityBreakdown buildBreakdown(UUID userId) {
        // Project activities (create, update, delete)
        List<ActivityType> projectTypes = Arrays.asList(
                ActivityType.CREATE_PROJECT,
                ActivityType.UPDATE_PROJECT,
                ActivityType.DELETE_PROJECT);
        long projects = userActivityRepository.countByUser_IdAndActivityTypeIn(userId, projectTypes);

        // Post activities (create, update, delete)
        List<ActivityType> postTypes = Arrays.asList(
                ActivityType.CREATE_POST,
                ActivityType.UPDATE_POST,
                ActivityType.DELETE_POST);
        long posts = userActivityRepository.countByUser_IdAndActivityTypeIn(userId, postTypes);

        // Comment activities (create, update, delete)
        List<ActivityType> commentTypes = Arrays.asList(
                ActivityType.CREATE_COMMENT,
                ActivityType.UPDATE_COMMENT,
                ActivityType.DELETE_COMMENT);
        long comments = userActivityRepository.countByUser_IdAndActivityTypeIn(userId, commentTypes);

        // Like activities (all like types)
        List<ActivityType> likeTypes = Arrays.asList(
                ActivityType.LIKE_PROJECT,
                ActivityType.LIKE_POST,
                ActivityType.LIKE_COMMENT);
        long likes = userActivityRepository.countByUser_IdAndActivityTypeIn(userId, likeTypes);

        return new ActivityBreakdown(projects, posts, comments, likes);
    }
}
