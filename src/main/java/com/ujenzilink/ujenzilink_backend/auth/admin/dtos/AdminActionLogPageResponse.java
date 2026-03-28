package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import java.util.List;

public record AdminActionLogPageResponse(
    List<AdminActionLogResponse> logs,
    String nextCursor,
    boolean hasMore
) {}
