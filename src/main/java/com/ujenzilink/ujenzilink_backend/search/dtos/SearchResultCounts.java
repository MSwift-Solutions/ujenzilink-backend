package com.ujenzilink.ujenzilink_backend.search.dtos;

/**
 * Holds the total match counts for each search category.
 * Allows the frontend to know the full result set size even when results are capped (max 10 each).
 */
public record SearchResultCounts(
        long totalPeople,
        long totalProjects,
        long totalPosts) {
}
