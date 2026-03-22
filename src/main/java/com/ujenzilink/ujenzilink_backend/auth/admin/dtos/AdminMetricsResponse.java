package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

public record AdminMetricsResponse(
    long totalUsers,
    long activeUsers,
    long deletedUsers,
    long totalProjects,
    long totalPosts,
    long activeUsersToday,
    long joinedToday,
    long joinedThisWeek
) {}
