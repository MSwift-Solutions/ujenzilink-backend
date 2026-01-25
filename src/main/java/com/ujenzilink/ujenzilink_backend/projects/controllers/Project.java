package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectResponse;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectDetailsResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectListResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectPostResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectDropdownsResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectImageResponse;
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
    public ResponseEntity<ApiCustomResponse<List<ProjectListResponse>>> getAllProjects() {
        ApiCustomResponse<List<ProjectListResponse>> response = projectService.getAllProjects();
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
}
