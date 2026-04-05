package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminProjectPageResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuditService;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminProjectSearchService;
import com.ujenzilink.ujenzilink_backend.projects.dtos.UpdateProjectPrivacyAdminRequest;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
@CrossOrigin
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class AdminProjectController {

    private final ProjectService projectService;
    private final AdminProjectSearchService adminProjectSearchService;
    private final AdminAuditService adminAuditService;
    private final HttpServletRequest httpServletRequest;

    public AdminProjectController(ProjectService projectService, 
                                  AdminProjectSearchService adminProjectSearchService,
                                  AdminAuditService adminAuditService,
                                  HttpServletRequest httpServletRequest) {
        this.projectService = projectService;
        this.adminProjectSearchService = adminProjectSearchService;
        this.adminAuditService = adminAuditService;
        this.httpServletRequest = httpServletRequest;
    }

    @PostMapping("/{projectId}/admin/privacy-status")
    public ResponseEntity<ApiCustomResponse<Void>> setProjectPrivacyAdmin(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectPrivacyAdminRequest request) {
        ApiCustomResponse<Void> response = projectService.setProjectPrivacyAdmin(projectId, request);
        if (response.statusCode() == 200) {
            String details = request.makePrivate() ? "Restricted project to private. Reason: " + request.reason() : "Lifted private restriction on project. Reason: " + request.reason();
            adminAuditService.logAction(
                AdminActionType.RESTRICT_PROJECT_PRIVACY,
                projectId.toString(),
                details,
                httpServletRequest
            );
        }
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/admin/search")
    public ResponseEntity<ApiCustomResponse<AdminProjectPageResponse>> searchProjectsAdmin(
            @RequestParam String query,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        ApiCustomResponse<AdminProjectPageResponse> response = adminProjectSearchService.searchProjectsAdmin(query,
                cursor, limit);
        if (response.statusCode() == 200) {
            adminAuditService.logAction(
                AdminActionType.ADMIN_PROJECT_SEARCH,
                "ALL",
                "Searched for projects with query: '" + query + "'",
                httpServletRequest
            );
        }
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
