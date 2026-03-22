package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.UserDeletionRequestResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminUserManagementService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/users")
@CrossOrigin
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminUserManagementController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping("/deletion-requests")
    public ResponseEntity<ApiCustomResponse<List<UserDeletionRequestResponse>>> getDeletionRequests() {
        ApiCustomResponse<List<UserDeletionRequestResponse>> response = adminUserManagementService.getUsersWithDeletionRequests();
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
