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

import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

        private final ImageRepository imageRepository;
        private final UserRepository userRepository;
        private final ImageValidationService imageValidationService;
        private final CloudinaryService cloudinaryService;
        private final SecurityUtil securityUtil;

        public ImageService(
                        ImageRepository imageRepository,
                        UserRepository userRepository,
                        ImageValidationService imageValidationService,
                        CloudinaryService cloudinaryService,
                        SecurityUtil securityUtil) {
                this.imageRepository = imageRepository;
                this.userRepository = userRepository;
                this.imageValidationService = imageValidationService;
                this.cloudinaryService = cloudinaryService;
                this.securityUtil = securityUtil;
        }

        @Transactional
        public ApiCustomResponse<String> uploadProfilePicture(MultipartFile file) {
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();

                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not authenticated or not found.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User user = userOpt.get();

                if (!user.getIsEnabled()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "Account not confirmed. Please confirm your account before uploading a profile picture.",
                                        HttpStatus.FORBIDDEN.value());
                }

                ImageMetadata metadata = imageValidationService.validateAndExtractMetadata(file);

                CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadImage(file);

                Image profileImage = new Image();
                profileImage.setUrl(uploadResponse.secureUrl());
                profileImage.setFilename(metadata.filename());
                profileImage.setFileType(metadata.fileType());
                profileImage.setFileSize(metadata.fileSize());
                profileImage.setWidth(uploadResponse.width());
                profileImage.setHeight(uploadResponse.height());
                profileImage.setUser(user);

                profileImage = imageRepository.save(profileImage);

                user.setProfilePicture(profileImage);
                userRepository.save(user);

                return new ApiCustomResponse<>(
                                uploadResponse.secureUrl(),
                                "Profile picture uploaded successfully.",
                                HttpStatus.OK.value());
        }

        @Transactional
        public ApiCustomResponse<Void> deleteImage(UUID imageId) {
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();

                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not authenticated or not found.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User user = userOpt.get();

                Image image = imageRepository.findById(imageId).orElse(null);

                if (image == null) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "Image not found.",
                                        HttpStatus.NOT_FOUND.value());
                }

                if (!image.getUser().getId().equals(user.getId())) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "You do not have permission to delete this image.",
                                        HttpStatus.FORBIDDEN.value());
                }

                image.setIsDeleted(true);
                image.setDeletedAt(java.time.Instant.now());
                imageRepository.save(image);

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
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();

                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not authenticated or not found.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User user = userOpt.get();
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
                Optional<User> userOpt = securityUtil.getAuthenticatedUser();

                if (userOpt.isEmpty()) {
                        return new ApiCustomResponse<>(
                                        null,
                                        "User not authenticated or not found.",
                                        HttpStatus.UNAUTHORIZED.value());
                }

                User targetUser = userRepository.findFirstByUsername(username);

                if (targetUser == null || targetUser.getIsDeleted()) {
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
