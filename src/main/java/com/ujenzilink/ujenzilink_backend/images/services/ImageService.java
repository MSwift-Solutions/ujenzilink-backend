package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryUploadResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import com.ujenzilink.ujenzilink_backend.images.dtos.ProfileImageResponse;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

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
                CloudinaryUploadResponse uploadResponse = cloudinaryService
                                .uploadImage(file);

                // 6. Create and populate Image entity
                Image profileImage = new Image();
                profileImage.setUrl(uploadResponse.secureUrl());
                profileImage.setFilename(metadata.filename());
                profileImage.setFileType(metadata.fileType());
                profileImage.setFileSize(metadata.fileSize());
                profileImage.setWidth(uploadResponse.width());
                profileImage.setHeight(uploadResponse.height());
                profileImage.setUser(user);

                // 7. Save the new image to the gallery (images table)
                profileImage = imageRepository.save(profileImage);

                // 8. Update the user's active pointer to this specific new image
                user.setProfilePicture(profileImage);
                userRepository.save(user);

                return new ApiCustomResponse<>(
                                uploadResponse.secureUrl(),
                                "Profile picture uploaded successfully.",
                                HttpStatus.OK.value());
        }

        @Transactional
        public ApiCustomResponse<Void> deleteImage(UUID imageId) {
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

                // 3. Retrieve image
                Image image = imageRepository.findById(imageId).orElse(null);

                if (image == null) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "Image not found.",
                                        HttpStatus.NOT_FOUND.value());
                }

                // 4. Verify ownership
                if (!image.getUser().getId().equals(user.getId())) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "You do not have permission to delete this image.",
                                        HttpStatus.FORBIDDEN.value());
                }

                // 5. Soft delete
                image.setIsDeleted(true);
                image.setDeletedAt(java.time.Instant.now());
                imageRepository.save(image);

                // 6. If it was the profile picture, remove the reference
                if (user.getProfilePicture() != null && user.getProfilePicture().getId().equals(imageId)) {
                        user.setProfilePicture(null);
                        userRepository.save(user);
                }

                return new ApiCustomResponse<>(
                                null,
                                "Image deleted successfully.",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<ProfileImageResponse> getMyProfileImage() {
                String currentUserEmail = SecurityUtil.getCurrentUsername();

                if (currentUserEmail == null) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not authenticated.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User user = userRepository.findFirstByEmail(currentUserEmail);

                if (user == null) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found.",
                                        HttpStatus.NOT_FOUND.value());
                }

                Image profilePicture = user.getProfilePicture();

                if (profilePicture == null || profilePicture.getIsDeleted()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "No profile picture found.",
                                        HttpStatus.NOT_FOUND.value());
                }

                ProfileImageResponse response = new ProfileImageResponse(
                                profilePicture.getId(),
                                profilePicture.getUrl(),
                                profilePicture.getFilename(),
                                profilePicture.getFileType(),
                                profilePicture.getFileSize(),
                                profilePicture.getWidth(),
                                profilePicture.getHeight(),
                                profilePicture.getUploadedAt());

                return new ApiCustomResponse<>(
                                response,
                                "Profile picture retrieved successfully.",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<ProfileImageResponse> getProfileImage(String username) {
                String currentUserEmail = SecurityUtil.getCurrentUsername();

                if (currentUserEmail == null) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not authenticated.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User targetUser = userRepository.findFirstByUsername(username);

                if (targetUser == null) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found.",
                                        HttpStatus.NOT_FOUND.value());
                }

                if (targetUser.getIsDeleted()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not found.",
                                        HttpStatus.NOT_FOUND.value());
                }

                Image profilePicture = targetUser.getProfilePicture();

                if (profilePicture == null || profilePicture.getIsDeleted()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "No profile picture found.",
                                        HttpStatus.NOT_FOUND.value());
                }

                ProfileImageResponse response = new ProfileImageResponse(
                                profilePicture.getId(),
                                profilePicture.getUrl(),
                                profilePicture.getFilename(),
                                profilePicture.getFileType(),
                                profilePicture.getFileSize(),
                                profilePicture.getWidth(),
                                profilePicture.getHeight(),
                                profilePicture.getUploadedAt());

                return new ApiCustomResponse<>(
                                response,
                                "Profile picture retrieved successfully.",
                                HttpStatus.OK.value());
        }
}
