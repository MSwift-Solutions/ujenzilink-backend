package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

import java.util.List;

public record AdminPostPageResponse(
    List<AdminPostResponse> posts,
    String nextCursor,
    boolean hasMore
) {}
