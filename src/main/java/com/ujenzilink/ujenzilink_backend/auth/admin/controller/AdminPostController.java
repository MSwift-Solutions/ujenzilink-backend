package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminPostPageResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.DeletePostAdminRequest;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminPostManagementService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/posts")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class AdminPostController {

    @Autowired
    private AdminPostManagementService adminPostManagementService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiCustomResponse<AdminPostPageResponse>> getPostsByUserId(
            @PathVariable UUID userId,
            @RequestParam(required = false) Integer limit) {
        ApiCustomResponse<AdminPostPageResponse> response = adminPostManagementService.getPostsByUserId(userId, limit);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiCustomResponse<Void>> deletePostByAdmin(
            @PathVariable UUID postId,
            @Valid @RequestBody DeletePostAdminRequest request) {
        ApiCustomResponse<Void> response = adminPostManagementService.deletePostByAdmin(postId, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
