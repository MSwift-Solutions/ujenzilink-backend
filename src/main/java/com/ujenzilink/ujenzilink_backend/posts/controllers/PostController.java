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
                                                .body(new ApiCustomResponse<>(null, errorMessage,
                                                                HttpStatus.BAD_REQUEST.value()));
                        }

                        ApiCustomResponse<CreatePostResponse> response = postService.createPost(request, images);
                        return ResponseEntity.status(response.statusCode()).body(response);

                } catch (JsonProcessingException e) {
                        return ResponseEntity.badRequest().body(new ApiCustomResponse<>(null,
                                        "Invalid JSON format: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
                }
        }

        @GetMapping
        public ResponseEntity<ApiCustomResponse<com.ujenzilink.ujenzilink_backend.posts.dtos.PostPageResponse>> getAllPosts(
                        @RequestParam(required = false) String cursor,
                        @RequestParam(required = false, defaultValue = "20") Integer size) {

                if (size < 1 || size > 100) {
                        return ResponseEntity.badRequest().body(
                                        new ApiCustomResponse<>(null, "Size must be between 1 and 100", 400));
                }

                ApiCustomResponse<com.ujenzilink.ujenzilink_backend.posts.dtos.PostPageResponse> response = postService
                                .getAllPosts(cursor, size);
                return ResponseEntity.status(response.statusCode()).body(response);
        }

        @GetMapping("/my-posts")
        public ResponseEntity<ApiCustomResponse<com.ujenzilink.ujenzilink_backend.posts.dtos.PostPageResponse>> getMyPosts(
                        @RequestParam(required = false) String cursor,
                        @RequestParam(required = false, defaultValue = "20") Integer size) {

                if (size < 1 || size > 100) {
                        return ResponseEntity.badRequest().body(
                                        new ApiCustomResponse<>(null, "Size must be between 1 and 100", 400));
                }

                ApiCustomResponse<com.ujenzilink.ujenzilink_backend.posts.dtos.PostPageResponse> response = postService
                                .getMyPosts(cursor, size);
                return ResponseEntity.status(response.statusCode()).body(response);
        }

        @GetMapping("/{postId}")
        public ResponseEntity<ApiCustomResponse<com.ujenzilink.ujenzilink_backend.posts.dtos.PostListResponse>> getPost(
                        @PathVariable java.util.UUID postId) {
                ApiCustomResponse<com.ujenzilink.ujenzilink_backend.posts.dtos.PostListResponse> response = postService
                                .getPost(postId);
                return ResponseEntity.status(response.statusCode()).body(response);
        }

        @PutMapping(value = "/{postId}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiCustomResponse<Void>> editPost(
                        @PathVariable java.util.UUID postId,
                        @RequestPart("request") String requestJson,
                        @RequestPart(value = "images", required = false) List<MultipartFile> images) {

                try {
                        com.ujenzilink.ujenzilink_backend.posts.dtos.EditPostRequest request = objectMapper.readValue(
                                        requestJson,
                                        com.ujenzilink.ujenzilink_backend.posts.dtos.EditPostRequest.class);

                        Set<ConstraintViolation<com.ujenzilink.ujenzilink_backend.posts.dtos.EditPostRequest>> violations = validator
                                        .validate(request);
                        if (!violations.isEmpty()) {
                                String errorMessage = violations.stream()
                                                .map(ConstraintViolation::getMessage)
                                                .collect(Collectors.joining(", "));
                                return ResponseEntity.badRequest()
                                                .body(new ApiCustomResponse<>(null, errorMessage,
                                                                HttpStatus.BAD_REQUEST.value()));
                        }

                        ApiCustomResponse<Void> response = postService.editPost(postId, request, images);
                        return ResponseEntity.status(response.statusCode()).body(response);

                } catch (JsonProcessingException e) {
                        return ResponseEntity.badRequest().body(new ApiCustomResponse<>(null,
                                        "Invalid JSON format: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
                }
        }

        @DeleteMapping("/{postId}")
        public ResponseEntity<ApiCustomResponse<Void>> deletePost(@PathVariable java.util.UUID postId) {
                ApiCustomResponse<Void> response = postService.deletePost(postId);
                return ResponseEntity.status(response.statusCode()).body(response);
        }

        @PostMapping("/{postId}/bookmark")
        public ResponseEntity<ApiCustomResponse<String>> toggleBookmark(@PathVariable java.util.UUID postId) {
                ApiCustomResponse<String> response = postService.toggleBookmark(postId);
                return ResponseEntity.status(response.statusCode()).body(response);
        }

        @GetMapping("/{postId}/bookmark-status")
        public ResponseEntity<ApiCustomResponse<Boolean>> checkBookmarkStatus(@PathVariable java.util.UUID postId) {
                ApiCustomResponse<Boolean> response = postService.checkBookmarkStatus(postId);
                return ResponseEntity.status(response.statusCode()).body(response);
        }

        @GetMapping("/bookmarked")
        public ResponseEntity<ApiCustomResponse<com.ujenzilink.ujenzilink_backend.posts.dtos.PostPageResponse>> getBookmarkedPosts(
                        @RequestParam(required = false) String cursor,
                        @RequestParam(required = false, defaultValue = "20") Integer size) {

                if (size < 1 || size > 100) {
                        return ResponseEntity.badRequest().body(
                                        new ApiCustomResponse<>(null, "Size must be between 1 and 100", 400));
                }

                ApiCustomResponse<com.ujenzilink.ujenzilink_backend.posts.dtos.PostPageResponse> response = postService
                                .getBookmarkedPosts(cursor, size);
                return ResponseEntity.status(response.statusCode()).body(response);
        }
}
