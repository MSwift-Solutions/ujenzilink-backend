package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.MemberRole;
import java.util.UUID;

public record ProjectMemberDTO(
                UUID id,
                CreatorInfoDTO user,
                MemberRole role,
                String lastActivity,
                boolean canManageStages,
                boolean canCreatePosts,
                boolean canUploadDocuments,
                boolean canManageMembers) {
}
