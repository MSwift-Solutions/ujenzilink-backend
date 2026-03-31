package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuditService;
import com.ujenzilink.ujenzilink_backend.images.dtos.HangingResourcesResponse;
import com.ujenzilink.ujenzilink_backend.images.services.CloudflareAdminService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/resources")
@CrossOrigin
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class ResourceManagementController {

    private final CloudflareAdminService cloudinaryAdminService;
    private final com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuditService adminAuditService;
    private final jakarta.servlet.http.HttpServletRequest httpServletRequest;

    public ResourceManagementController(CloudflareAdminService cloudinaryAdminService,
                                        AdminAuditService adminAuditService,
                                        HttpServletRequest httpServletRequest) {
        this.cloudinaryAdminService = cloudinaryAdminService;
        this.adminAuditService = adminAuditService;
        this.httpServletRequest = httpServletRequest;
    }

    // --- User Profile Pictures ---
    @GetMapping("/hanging/users/orphaned")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getOrphanedProfilePictures() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getOrphanedProfilePictures(),
                "Orphaned profile pictures retrieved successfully",
                HttpStatus.OK.value()));
    }

    @GetMapping("/hanging/users/flagged")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getFlaggedProfilePictures() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getFlaggedProfilePictures(),
                "Flagged profile pictures retrieved successfully",
                HttpStatus.OK.value()));
    }

    @GetMapping("/hanging/users/parent-deleted")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getParentDeletedProfilePictures() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getParentDeletedProfilePictures(),
                "Profile pictures with deleted owners retrieved successfully",
                HttpStatus.OK.value()));
    }

    // --- Post Images ---
    @GetMapping("/hanging/posts/orphaned")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getOrphanedPostImages() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getOrphanedPostImages(),
                "Orphaned post images retrieved successfully",
                HttpStatus.OK.value()));
    }

    @GetMapping("/hanging/posts/flagged")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getFlaggedPostImages() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getFlaggedPostImages(),
                "Flagged post images retrieved successfully",
                HttpStatus.OK.value()));
    }

    @GetMapping("/hanging/posts/parent-deleted")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getParentDeletedPostImages() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getParentDeletedPostImages(),
                "Post images with deleted posts retrieved successfully",
                HttpStatus.OK.value()));
    }

    // --- Project Stage Images ---
    @GetMapping("/hanging/projects/orphaned")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getOrphanedProjectImages() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getOrphanedProjectImages(),
                "Orphaned project images retrieved successfully",
                HttpStatus.OK.value()));
    }

    @GetMapping("/hanging/projects/flagged")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getFlaggedProjectImages() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getFlaggedProjectImages(),
                "Flagged project images retrieved successfully",
                HttpStatus.OK.value()));
    }

    @GetMapping("/hanging/projects/parent-deleted")
    public ResponseEntity<ApiCustomResponse<HangingResourcesResponse>> getParentDeletedProjectImages() {
        return ResponseEntity.ok(new ApiCustomResponse<>(
                cloudinaryAdminService.getParentDeletedProjectImages(),
                "Project images with deleted projects retrieved successfully",
                HttpStatus.OK.value()));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiCustomResponse<java.util.Map<String, String>>> deleteResources(@RequestBody List<String> publicIds) {
        Map<String, String> results = cloudinaryAdminService.deleteResources(publicIds);
        
        adminAuditService.logAction(
            AdminActionType.BULK_DELETE_RESOURCES,
            "BULK-" + publicIds.size(),
            "Bulk deleted " + publicIds.size() + " resources from cloud storage",
            httpServletRequest
        );

        return ResponseEntity.ok(new ApiCustomResponse<>(
                results,
                "Bulk deletion processed (" + publicIds.size() + " files total)",
                HttpStatus.OK.value()));
    }
}
