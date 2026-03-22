package com.ujenzilink.ujenzilink_backend.user_mgt.repositories;

import com.ujenzilink.ujenzilink_backend.user_mgt.enums.ActivityType;
import com.ujenzilink.ujenzilink_backend.user_mgt.models.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {

    // Count total activities for a user
    long countByUser_Id(UUID userId);

    // Get all distinct activity dates for a user (for streak calculation)
    @Query("SELECT DISTINCT ua.activityDate FROM UserActivity ua WHERE ua.user.id = :userId ORDER BY ua.activityDate ASC")
    List<LocalDate> findDistinctActivityDatesByUserId(@Param("userId") UUID userId);

    // Get activities for a user within a date range
    List<UserActivity> findByUser_IdAndActivityDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    // Get all activities for a user
    List<UserActivity> findByUser_Id(UUID userId);

    // Count activities by type for a user
    long countByUser_IdAndActivityType(UUID userId, ActivityType activityType);

    // Count activities by multiple types for a user
    long countByUser_IdAndActivityTypeIn(UUID userId, List<ActivityType> activityTypes);

    // Get activities for a user for a specific date
    List<UserActivity> findByUser_IdAndActivityDate(UUID userId, LocalDate date);

    @Query("SELECT COUNT(DISTINCT ua.user.id) FROM UserActivity ua WHERE ua.activityDate = :date")
    long countActiveUsersByDate(@Param("date") LocalDate date);
}
