package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

import java.util.List;
import java.util.UUID;

public record UserSummaryResponse(
        UUID userId,
        String displayName,
        String username,
        String location,
        String email,
        String phone,
        String profileImageUrl,
        List<SocialLink> socialLinks) {
}
