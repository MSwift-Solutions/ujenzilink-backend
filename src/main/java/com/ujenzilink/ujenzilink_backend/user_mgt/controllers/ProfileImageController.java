package com.ujenzilink.ujenzilink_backend.user_mgt.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ProfileImageResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.ProfileImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/images")
@CrossOrigin
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    public ProfileImageController(ProfileImageService profileImageService) {
        this.profileImageService = profileImageService;
    }

    @PostMapping(value = "/upload-profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<String>> uploadProfilePicture(
            @RequestPart("profilePicture") MultipartFile file) {

        ApiCustomResponse<String> response = profileImageService.uploadProfilePicture(file);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @PatchMapping(value = "/change-profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<String>> changeProfilePicture(
            @RequestPart("profilePicture") MultipartFile file) {

        ApiCustomResponse<String> response = profileImageService.changeProfilePicture(file);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiCustomResponse<Void>> deleteImage(@PathVariable UUID imageId) {
        ApiCustomResponse<Void> response = profileImageService.deleteImage(imageId);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/profile-picture/me")
    public ResponseEntity<ApiCustomResponse<ProfileImageResponse>> getMyProfile() {
        ApiCustomResponse<ProfileImageResponse> response = profileImageService.getMyProfile();

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/profile-picture/{username}")
    public ResponseEntity<ApiCustomResponse<ProfileImageResponse>> getProfileImage(
            @PathVariable String username) {
        ApiCustomResponse<ProfileImageResponse> response = profileImageService.getProfileImage(username);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }
}
