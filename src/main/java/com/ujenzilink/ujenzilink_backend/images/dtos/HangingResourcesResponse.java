package com.ujenzilink.ujenzilink_backend.images.dtos;

import java.util.List;

public record HangingResourcesResponse(
        List<CloudinaryResourceDTO> resources,
        int totalCount,
        long totalSizeInBytes
) {}
