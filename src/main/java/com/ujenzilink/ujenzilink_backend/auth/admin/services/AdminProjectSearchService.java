package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminProjectPageResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminProjectResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.repos.AdminProjectSearchRepo;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminProjectSearchService {

    @Autowired
    private AdminProjectSearchRepo adminProjectSearchRepo;

    @Autowired
    private SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public ApiCustomResponse<AdminProjectPageResponse> searchProjectsAdmin(String query, String cursor, Integer limit) {
        // ── Auth & Role Check ───────────────────────────────────────────────
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }
        User currentUser = userOpt.get();
        if (currentUser.getRole() != Roles.ROLE_ADMIN && currentUser.getRole() != Roles.ROLE_SUPER_ADMIN) {
            return new ApiCustomResponse<>(null, "Access denied. Admin privileges required.", HttpStatus.FORBIDDEN.value());
        }

        // ── Validation ──────────────────────────────────────────────────────
        if (query == null || query.isBlank()) {
            return new ApiCustomResponse<>(null, "Search query must not be blank", HttpStatus.BAD_REQUEST.value());
        }

        String sanitised = query.trim().replaceAll("[!&|<>():*]", " ").trim();
        int effectiveLimit = (limit == null || limit < 1) ? 10 : Math.min(limit, 50);

        // ── Cursor ──────────────────────────────────────────────────────────
        Instant cursorTime = Instant.now();
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decodedJson = new String(Base64.getDecoder().decode(cursor));
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> cursorData = mapper.readValue(decodedJson, Map.class);
                if (cursorData.containsKey("timestamp")) {
                    cursorTime = Instant.parse((String) cursorData.get("timestamp"));
                }
            } catch (Exception e) {
                return new ApiCustomResponse<>(null, "Invalid cursor format", HttpStatus.BAD_REQUEST.value());
            }
        }

        // ── Search ──────────────────────────────────────────────────────────
        List<Project> projects = adminProjectSearchRepo.searchProjectsAdminPaginated(sanitised, sanitised, effectiveLimit + 1, cursorTime);

        boolean hasMore = projects.size() > effectiveLimit;
        if (hasMore) {
            projects = projects.subList(0, effectiveLimit);
        }

        long totalProjects = adminProjectSearchRepo.countSearchProjectsAdminPaginated(sanitised, sanitised);

        // ── Map ─────────────────────────────────────────────────────────────
        List<AdminProjectResponse> projectResponses = projects.stream()
                .map(this::mapToAdminProjectResponse)
                .collect(Collectors.toList());

        // ── Cursor generation ───────────────────────────────────────────────
        String nextCursor = null;
        if (hasMore && !projects.isEmpty()) {
            try {
                Project lastProject = projects.get(projects.size() - 1);
                String cursorJson = String.format("{\"timestamp\":\"%s\"}", lastProject.getCreatedAt().toString());
                nextCursor = Base64.getEncoder().encodeToString(cursorJson.getBytes());
            } catch (Exception e) {
                // Ignore cursor generation errors
            }
        }

        AdminProjectPageResponse pageResponse = new AdminProjectPageResponse(projectResponses, nextCursor, hasMore, totalProjects);
        return new ApiCustomResponse<>(pageResponse, "Admin project search results retrieved successfully", HttpStatus.OK.value());
    }

    private AdminProjectResponse mapToAdminProjectResponse(Project project) {
        User owner = project.getOwner();
        String ownerName = (owner != null) ? owner.getFullName() : "Unknown";
        String profilePictureUrl = (owner != null && owner.getProfilePicture() != null)
                ? owner.getProfilePicture().getUrl()
                : "https://ui-avatars.com/api/?name=" + ownerName.replace(" ", "+") + "&background=random";
        String username = (owner != null) 
                ? ((owner.getUserHandle() != null && !owner.getUserHandle().isEmpty()) ? owner.getUserHandle() : owner.getEmail())
                : "unknown";
        
        CreatorInfoDTO ownerInfo = new CreatorInfoDTO(owner != null ? owner.getId() : null, ownerName, username, profilePictureUrl);

        return new AdminProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getProjectType(),
                project.getProjectStatus(),
                project.getVisibility(),
                project.getLocation(),
                project.getCreatedAt(),
                ownerInfo,
                project.getEstimatedBudget(),
                project.getContractValue(),
                project.getCurrency(),
                project.isAdminPrivate(),
                project.getAdminPrivateReason(),
                project.getAdminPrivateBy() != null ? project.getAdminPrivateBy().getUserHandle() : null
        );
    }
}
