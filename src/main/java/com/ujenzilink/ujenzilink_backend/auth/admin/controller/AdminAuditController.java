package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminActionLogPageResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuditService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/audit")
@CrossOrigin
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    public AdminAuditController(AdminAuditService adminAuditService) {
        this.adminAuditService = adminAuditService;
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiCustomResponse<AdminActionLogPageResponse>> getAuditLogs(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size) {
        
        AdminActionLogPageResponse response = adminAuditService.getAuditLogs(cursor, size);
        return ResponseEntity.ok(new ApiCustomResponse<>(response, "Audit logs retrieved successfully", 200));
    }
}
