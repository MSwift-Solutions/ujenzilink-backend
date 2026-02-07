package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.*;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
public class Project {

    private final ProjectService projectService;

    public Project(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiCustomResponse<CreateProjectResponse>> createProject(
            @RequestBody @Valid CreateProjectRequest request) {

        ApiCustomResponse<CreateProjectResponse> response = projectService.createProject(request);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<ApiCustomResponse<com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse>> getAllProjects(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        // Validate size
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(
                    new ApiCustomResponse<>(null, "Size must be between 1 and 100", 400));
        }

        ApiCustomResponse<ProjectPageResponse> response = projectService
                .getAllProjects(cursor, size);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/my-projects")
    public ResponseEntity<ApiCustomResponse<com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse>> getMyProjects(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        // Validate size
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(
                    new ApiCustomResponse<>(null, "Size must be between 1 and 100", 400));
        }

        ApiCustomResponse<ProjectPageResponse> response = projectService
                .getMyProjects(cursor, size);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/followed-projects")
    public ResponseEntity<ApiCustomResponse<com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPageResponse>> getFollowedProjects(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        // Validate size
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(
                    new ApiCustomResponse<>(null, "Size must be between 1 and 100", 400));
        }

        ApiCustomResponse<ProjectPageResponse> response = projectService
                .getFollowedProjects(cursor, size);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiCustomResponse<ProjectDetailsResponse>> getProjectDetails(
            @PathVariable UUID projectId) {
        ApiCustomResponse<ProjectDetailsResponse> response = projectService.getProjectDetails(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/posts")
    public ResponseEntity<ApiCustomResponse<List<ProjectPostResponse>>> getProjectPosts(
            @PathVariable UUID projectId) {
        ApiCustomResponse<List<ProjectPostResponse>> response = projectService.getProjectPosts(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/latest-posts")
    public ResponseEntity<ApiCustomResponse<List<ProjectPostResponse>>> getLatestProjectPosts(
            @PathVariable UUID projectId) {
        ApiCustomResponse<List<ProjectPostResponse>> response = projectService.getLatestProjectPosts(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/posts-count")
    public ResponseEntity<ApiCustomResponse<Long>> getProjectPostsCount(
            @PathVariable UUID projectId) {
        ApiCustomResponse<Long> response = projectService.getProjectPostsCount(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/images-count")
    public ResponseEntity<ApiCustomResponse<Long>> getProjectImagesCount(
            @PathVariable UUID projectId) {
        ApiCustomResponse<Long> response = projectService.getProjectImagesCount(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/images")
    public ResponseEntity<ApiCustomResponse<List<ProjectImageResponse>>> getProjectImages(
            @PathVariable UUID projectId) {
        ApiCustomResponse<List<ProjectImageResponse>> response = projectService.getProjectImages(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/dropdowns")
    public ResponseEntity<ApiCustomResponse<ProjectDropdownsResponse>> getProjectDropdowns() {
        ApiCustomResponse<ProjectDropdownsResponse> response = projectService.getProjectDropdowns();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/{projectId}/delete")
    public ResponseEntity<ApiCustomResponse<Void>> deleteProject(@PathVariable UUID projectId) {
        ApiCustomResponse<Void> response = projectService.deleteProject(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PatchMapping("/{projectId}/visibility")
    public ResponseEntity<ApiCustomResponse<Void>> updateProjectVisibility(
            @PathVariable UUID projectId,
            @RequestBody @Valid UpdateProjectVisibilityRequest request) {
        ApiCustomResponse<Void> response = projectService.updateProjectVisibility(projectId, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/visibility")
    public ResponseEntity<ApiCustomResponse<ProjectVisibilityResponse>> getProjectVisibility(
            @PathVariable UUID projectId) {
        ApiCustomResponse<ProjectVisibilityResponse> response = projectService.getProjectVisibility(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PutMapping("/{projectId}/edit")
    public ResponseEntity<ApiCustomResponse<Void>> editProject(
            @PathVariable UUID projectId,
            @RequestBody @Valid EditProjectRequest request) {
        ApiCustomResponse<Void> response = projectService.editProject(projectId, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/editable-data")
    public ResponseEntity<ApiCustomResponse<EditProjectRequest>> getEditableProjectData(
            @PathVariable UUID projectId) {
        ApiCustomResponse<EditProjectRequest> response = projectService.getEditableProjectData(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
