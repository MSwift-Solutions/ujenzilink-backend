package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectStageRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateProjectStageResponse;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectStageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/projects/stages")
public class ProjectStage {

    private final ProjectStageService projectStageService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ProjectStage(ProjectStageService projectStageService, ObjectMapper objectMapper) {
        this.projectStageService = projectStageService;
        this.objectMapper = objectMapper;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<CreateProjectStageResponse>> createProjectStage(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            // Deserialize JSON string to DTO
            CreateProjectStageRequest request = objectMapper.readValue(requestJson, CreateProjectStageRequest.class);

            // Manual validation
            Set<ConstraintViolation<CreateProjectStageRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                return ResponseEntity.badRequest()
                        .body(new ApiCustomResponse<>(null, errorMessage, HttpStatus.BAD_REQUEST.value()));
            }

            ApiCustomResponse<CreateProjectStageResponse> response = projectStageService.createProjectStage(request,
                    images);
            return ResponseEntity.status(response.statusCode()).body(response);

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(new ApiCustomResponse<>(null,
                    "Invalid JSON format: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}
