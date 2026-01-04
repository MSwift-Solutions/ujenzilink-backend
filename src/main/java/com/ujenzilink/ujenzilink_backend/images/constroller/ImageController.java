package com.ujenzilink.ujenzilink_backend.images.constroller;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.services.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/images")
@CrossOrigin
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiCustomResponse<String>> uploadProfilePicture(
            @RequestPart("profilePicture") MultipartFile file) {

        ApiCustomResponse<String> response = imageService.uploadProfilePicture(file);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }
}
