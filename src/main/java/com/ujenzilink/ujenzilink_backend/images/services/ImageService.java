package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ImageValidationService imageValidationService;
    private final CloudinaryService cloudinaryService;

    public ImageService(
            ImageRepository imageRepository,
            UserRepository userRepository,
            ImageValidationService imageValidationService,
            CloudinaryService cloudinaryService) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.imageValidationService = imageValidationService;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public ApiCustomResponse<String> uploadProfilePicture(MultipartFile file) {
        // 1. Validate user authentication
        String currentUserEmail = SecurityUtil.getCurrentUsername();

        if (currentUserEmail == null) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        // 2. Retrieve user from database
        User user = userRepository.findFirstByEmail(currentUserEmail);

        if (user == null) {
            return new ApiCustomResponse<>(
                    null,
                    "User not found.",
                    HttpStatus.NOT_FOUND.value());
        }

        // 3. Verify account is enabled
        if (!user.getIsEnabled()) {
            return new ApiCustomResponse<>(
                    null,
                    "Account not confirmed. Please confirm your account before uploading a profile picture.",
                    HttpStatus.FORBIDDEN.value());
        }

        // 4. Validate image and extract metadata
        ImageMetadata metadata = imageValidationService.validateAndExtractMetadata(file);

        // 5. Upload to Cloudinary
        String cloudinaryUrl = cloudinaryService.uploadImage(file);

        // 6. Create and populate Image entity
        Image profileImage = new Image();
        profileImage.setUrl(cloudinaryUrl);
        profileImage.setFilename(metadata.filename());
        profileImage.setFileType(metadata.fileType());
        profileImage.setFileSize(metadata.fileSize());
        profileImage.setWidth(metadata.width());
        profileImage.setHeight(metadata.height());
        profileImage.setUser(user);

        // 7. Save the new image to the gallery (images table)
        profileImage = imageRepository.save(profileImage);

        // 8. Update the user's active pointer to this specific new image
        user.setProfilePicture(profileImage);
        userRepository.save(user);

        return new ApiCustomResponse<>(
                cloudinaryUrl,
                "Profile picture uploaded successfully.",
                HttpStatus.OK.value());
    }
}
