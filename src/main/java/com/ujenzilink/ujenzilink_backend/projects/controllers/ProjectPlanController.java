package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.PlanFileResponse;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectPlanFileService;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanVisibility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/v1/projects/plans")
public class ProjectPlanController {

    private final ProjectPlanFileService planFileService;

    public ProjectPlanController(ProjectPlanFileService planFileService) {
        this.planFileService = planFileService;
    }

    @PostMapping(value = "/{projectId}/plans", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<PlanFileResponse>> createPlanAndUploadFile(
            @PathVariable UUID projectId,
            @RequestPart("file") MultipartFile file,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam PlanVisibility visibility,
            @RequestParam(required = false) String displayLabel) {

        ApiCustomResponse<PlanFileResponse> response =
                planFileService.createPlanAndUploadFile(projectId, file, name, description, price, visibility, displayLabel);

        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{planId}/has-paid")
    public ResponseEntity<ApiCustomResponse<Boolean>> hasUserPaidForPlan(@PathVariable UUID planId) {
        ApiCustomResponse<Boolean> response = planFileService.hasUserPaidForPlan(planId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiCustomResponse<Void>> deletePlanFile(@PathVariable UUID fileId) {
        ApiCustomResponse<Void> response = planFileService.deletePlanFile(fileId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
