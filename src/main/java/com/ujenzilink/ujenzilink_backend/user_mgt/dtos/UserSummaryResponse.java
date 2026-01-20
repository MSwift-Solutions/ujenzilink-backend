package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

import java.util.List;

public record UserSummaryResponse(
        String displayName,
        String username,
        String location,
        String email,
        String phone,
        String profileImageUrl,
        List<SocialLink> socialLinks) {
}
