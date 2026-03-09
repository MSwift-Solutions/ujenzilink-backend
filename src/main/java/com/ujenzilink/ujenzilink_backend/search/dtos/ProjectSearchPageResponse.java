package com.ujenzilink.ujenzilink_backend.search.dtos;

import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectListResponse;

import java.util.List;

public record ProjectSearchPageResponse(
        List<ProjectListResponse> projects,
        String nextCursor,
        boolean hasMore,
        long totalProjects) {
}
