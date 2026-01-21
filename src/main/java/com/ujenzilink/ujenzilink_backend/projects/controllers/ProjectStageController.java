package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectStageRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectStageResponse;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectStageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/projects/stages")
public class ProjectStageController {

    private final ProjectStageService projectStageService;

    public ProjectStageController(ProjectStageService projectStageService) {
        this.projectStageService = projectStageService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<CreateProjectStageResponse>> createProjectStage(
            @RequestPart("request") @Valid CreateProjectStageRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        ApiCustomResponse<CreateProjectStageResponse> response = projectStageService.createProjectStage(request,
                images);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }
}
