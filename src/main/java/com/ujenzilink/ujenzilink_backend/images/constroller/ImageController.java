package com.ujenzilink.ujenzilink_backend.images.constroller;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ProfileImageResponse;
import com.ujenzilink.ujenzilink_backend.images.services.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/images")
@CrossOrigin
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/upload-profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<String>> uploadProfilePicture(
            @RequestPart("profilePicture") MultipartFile file) {

        ApiCustomResponse<String> response = imageService.uploadProfilePicture(file);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @PatchMapping(value = "/change-profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<String>> changeProfilePicture(
            @RequestPart("profilePicture") MultipartFile file) {

        ApiCustomResponse<String> response = imageService.changeProfilePicture(file);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiCustomResponse<Void>> deleteImage(@PathVariable UUID imageId) {
        ApiCustomResponse<Void> response = imageService.deleteImage(imageId);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/profile-picture/me")
    public ResponseEntity<ApiCustomResponse<ProfileImageResponse>> getMyProfile() {
        ApiCustomResponse<ProfileImageResponse> response = imageService.getMyProfile();

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/profile-picture/{username}")
    public ResponseEntity<ApiCustomResponse<ProfileImageResponse>> getProfileImage(
            @PathVariable String username) {
        ApiCustomResponse<ProfileImageResponse> response = imageService.getProfileImage(username);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }
}
