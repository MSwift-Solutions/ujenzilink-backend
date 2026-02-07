package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

import java.util.Map;

public record UserActivityStatsResponse(
        long totalActivities,
        long activeDays,
        long maxStreak,
        ActivityBreakdown breakdown,
        Map<String, Integer> dailyStats) {
}
