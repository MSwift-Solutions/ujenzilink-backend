package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.UpdateProjectPrivacyAdminRequest;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
public class AdminProjectController {

    private final ProjectService projectService;

    public AdminProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/{projectId}/admin/privacy-status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiCustomResponse<Void>> setProjectPrivacyAdmin(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectPrivacyAdminRequest request) {
        ApiCustomResponse<Void> response = projectService.setProjectPrivacyAdmin(projectId, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
