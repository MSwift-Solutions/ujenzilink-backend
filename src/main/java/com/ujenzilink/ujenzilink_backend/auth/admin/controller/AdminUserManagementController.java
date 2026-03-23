package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminMetricsResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UserDeletionRequestResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminUserManagementService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/users")
@CrossOrigin
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminUserManagementController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiCustomResponse<AdminMetricsResponse>> getMetrics() {
        ApiCustomResponse<AdminMetricsResponse> response = adminUserManagementService.getAdminMetrics();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/deletion-requests")
    public ResponseEntity<ApiCustomResponse<List<UserDeletionRequestResponse>>> getDeletionRequests() {
        ApiCustomResponse<List<UserDeletionRequestResponse>> response = adminUserManagementService.getUsersWithDeletionRequests();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/revert-deletion/{userId}")
    public ResponseEntity<ApiCustomResponse<String>> revertDeletion(@PathVariable UUID userId) {
        ApiCustomResponse<String> response = adminUserManagementService.revertUserDeletion(userId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
