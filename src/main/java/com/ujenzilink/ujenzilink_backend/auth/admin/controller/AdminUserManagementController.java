package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminMetricsResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.SuspendedUserResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UnverifiedUserResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UserDeletionRequestResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuditService;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminUserManagementService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AdminAuditService adminAuditService;
    private final HttpServletRequest httpServletRequest;

    public AdminUserManagementController(AdminUserManagementService adminUserManagementService,
                                         AdminAuditService adminAuditService,
                                            HttpServletRequest httpServletRequest) {
        this.adminUserManagementService = adminUserManagementService;
        this.adminAuditService = adminAuditService;
        this.httpServletRequest = httpServletRequest;
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
        if (response.statusCode() == 200) {
            adminAuditService.logAction(
                com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType.REVERT_USER_DELETION,
                userId.toString(),
                "Reverted user deletion request: " + response.message(),
                httpServletRequest
            );
        }
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/unverified")
    public ResponseEntity<ApiCustomResponse<List<UnverifiedUserResponse>>> getUnverifiedUsers() {
        ApiCustomResponse<List<UnverifiedUserResponse>> response = adminUserManagementService.getUnverifiedUsers();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<ApiCustomResponse<String>> verifyUser(@PathVariable UUID userId) {
        ApiCustomResponse<String> response = adminUserManagementService.verifyUserByAdmin(userId);
        if (response.statusCode() == 200) {
            adminAuditService.logAction(
                AdminActionType.VERIFY_USER,
                userId.toString(),
                "Manually verified user account: " + response.message(),
                httpServletRequest
            );
        }
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/suspend/{userId}")
    public ResponseEntity<ApiCustomResponse<String>> suspendUser(
            @PathVariable UUID userId,
            @jakarta.validation.Valid @RequestBody com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UserSuspensionRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.ujenzilink.ujenzilink_backend.auth.models.User adminUser) {
        ApiCustomResponse<String> response = adminUserManagementService.suspendUser(userId, request.reason(), adminUser);
        if (response.statusCode() == 200) {
            adminAuditService.logAction(
                AdminActionType.SUSPEND_USER,
                userId.toString(),
                "Suspended user account with reason: " + request.reason(),
                httpServletRequest
            );
        }
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/unsuspend/{userId}")
    public ResponseEntity<ApiCustomResponse<String>> unsuspendUser(
            @PathVariable UUID userId,
            @jakarta.validation.Valid @RequestBody com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UserSuspensionRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.ujenzilink.ujenzilink_backend.auth.models.User adminUser) {
        ApiCustomResponse<String> response = adminUserManagementService.unsuspendUser(userId, request.reason(), adminUser);
        if (response.statusCode() == 200) {
            adminAuditService.logAction(
                AdminActionType.UNSUSPEND_USER,
                userId.toString(),
                "Unsuspended user account with reason: " + request.reason(),
                httpServletRequest
            );
        }
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/suspended")
    public ResponseEntity<ApiCustomResponse<List<SuspendedUserResponse>>> getSuspendedUsers() {
        ApiCustomResponse<List<SuspendedUserResponse>> response = adminUserManagementService.getSuspendedUsers();
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
