package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.MemberRole;
import java.util.UUID;

public record AddMemberRequest(UUID userId, MemberRole role) {}
