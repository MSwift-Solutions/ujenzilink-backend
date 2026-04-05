package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import java.util.List;

public record AdminProjectPageResponse(
        List<AdminProjectResponse> projects,
        String nextCursor,
        boolean hasMore,
        long totalProjects
) {}
