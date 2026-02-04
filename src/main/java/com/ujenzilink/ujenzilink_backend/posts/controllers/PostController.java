package com.ujenzilink.ujenzilink_backend.posts.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.posts.dtos.CreatePostRequest;
import com.ujenzilink.ujenzilink_backend.posts.dtos.CreatePostResponse;
import com.ujenzilink.ujenzilink_backend.posts.services.PostService;
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
@RequestMapping("/v1/posts")
public class PostController {

    private final PostService postService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public PostController(PostService postService, ObjectMapper objectMapper) {
        this.postService = postService;
        this.objectMapper = objectMapper;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<CreatePostResponse>> createPost(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            CreatePostRequest request = objectMapper.readValue(requestJson, CreatePostRequest.class);

            Set<ConstraintViolation<CreatePostRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                return ResponseEntity.badRequest()
                        .body(new ApiCustomResponse<>(null, errorMessage, HttpStatus.BAD_REQUEST.value()));
            }

            ApiCustomResponse<CreatePostResponse> response = postService.createPost(request, images);
            return ResponseEntity.status(response.statusCode()).body(response);

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(new ApiCustomResponse<>(null,
                    "Invalid JSON format: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}
