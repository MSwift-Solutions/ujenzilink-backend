package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

public record UserStatsResponse(
                long totalPosts,
                long totalProjects,
                long totalComments,
                long totalLikes) {
}
