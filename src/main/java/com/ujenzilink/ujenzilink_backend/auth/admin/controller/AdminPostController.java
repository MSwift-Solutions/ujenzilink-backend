package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminPostPageResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.DeletePostAdminRequest;
import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuditService;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminPostManagementService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/posts")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class AdminPostController {

    private final AdminPostManagementService adminPostManagementService;
    private final AdminAuditService adminAuditService;
    private final HttpServletRequest httpServletRequest;

    public AdminPostController(AdminPostManagementService adminPostManagementService,
                               AdminAuditService adminAuditService,
                               HttpServletRequest httpServletRequest) {
        this.adminPostManagementService = adminPostManagementService;
        this.adminAuditService = adminAuditService;
        this.httpServletRequest = httpServletRequest;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiCustomResponse<AdminPostPageResponse>> getPostsByUserId(
            @PathVariable UUID userId,
            @RequestParam(required = false) Integer limit) {
        ApiCustomResponse<AdminPostPageResponse> response = adminPostManagementService.getPostsByUserId(userId, limit);
        if (response.statusCode() == 200) {
            adminAuditService.logAction(
                AdminActionType.ADMIN_USER_POSTS_VIEW,
                userId.toString(),
                "Viewed posts for user with ID: " + userId,
                httpServletRequest
            );
        }
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiCustomResponse<Void>> deletePostByAdmin(
            @PathVariable UUID postId,
            @Valid @RequestBody DeletePostAdminRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.ujenzilink.ujenzilink_backend.auth.models.User adminUser) {
        ApiCustomResponse<Void> response = adminPostManagementService.deletePostByAdmin(postId, request, adminUser);
        if (response.statusCode() == 200) {
            adminAuditService.logAction(
                AdminActionType.DELETE_POST_ADMIN,
                postId.toString(),
                "Deleted post by admin. Reason: " + request.reason(),
                httpServletRequest
            );
        }
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
