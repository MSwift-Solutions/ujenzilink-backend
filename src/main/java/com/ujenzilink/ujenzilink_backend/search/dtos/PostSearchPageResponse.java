package com.ujenzilink.ujenzilink_backend.search.dtos;

import com.ujenzilink.ujenzilink_backend.posts.dtos.PostListResponse;

import java.util.List;

public record PostSearchPageResponse(
        List<PostListResponse> posts,
        String nextCursor,
        boolean hasMore,
        long totalPosts) {
}
