package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminProjectPageResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminProjectSearchService;
import com.ujenzilink.ujenzilink_backend.projects.dtos.UpdateProjectPrivacyAdminRequest;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class AdminProjectController {

    private final ProjectService projectService;
    private final AdminProjectSearchService adminProjectSearchService;

    public AdminProjectController(ProjectService projectService, AdminProjectSearchService adminProjectSearchService) {
        this.projectService = projectService;
        this.adminProjectSearchService = adminProjectSearchService;
    }

    @PostMapping("/{projectId}/admin/privacy-status")
    public ResponseEntity<ApiCustomResponse<Void>> setProjectPrivacyAdmin(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectPrivacyAdminRequest request) {
        ApiCustomResponse<Void> response = projectService.setProjectPrivacyAdmin(projectId, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/admin/search")
    public ResponseEntity<ApiCustomResponse<AdminProjectPageResponse>> searchProjectsAdmin(
            @RequestParam String query,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        ApiCustomResponse<AdminProjectPageResponse> response = adminProjectSearchService.searchProjectsAdmin(query,
                cursor, limit);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
