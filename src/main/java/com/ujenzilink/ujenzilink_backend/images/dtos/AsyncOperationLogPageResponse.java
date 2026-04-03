package com.ujenzilink.ujenzilink_backend.images.dtos;

import java.util.List;

public record AsyncOperationLogPageResponse(
        List<AsyncOperationLogDTO> items,
        String nextCursor,
        boolean hasMore,
        int totalFailed,
        int totalRetried
) {}
