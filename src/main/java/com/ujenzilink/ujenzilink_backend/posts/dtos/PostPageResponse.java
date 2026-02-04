package com.ujenzilink.ujenzilink_backend.posts.dtos;

import java.util.List;

public record PostPageResponse(
        List<PostListResponse> posts,
        String nextCursor,
        boolean hasMore) {
}
