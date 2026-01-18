package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectResponse;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectListResponse;
import java.util.List;

@RestController
@RequestMapping("/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
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
}
