package com.ujenzilink.ujenzilink_backend.images.constroller;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ProfilePictureRequest;
import com.ujenzilink.ujenzilink_backend.images.services.ImageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/images")
@CrossOrigin
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<ApiCustomResponse<String>> uploadProfilePicture(
            @RequestBody @Valid ProfilePictureRequest request) {
        
        ApiCustomResponse<String> response = imageService.uploadProfilePicture(request.profilePicture());
        
        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }
}

