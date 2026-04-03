package com.ujenzilink.ujenzilink_backend.user_mgt.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import com.ujenzilink.ujenzilink_backend.images.dtos.R2UploadResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.ProfileImageResponse;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import com.ujenzilink.ujenzilink_backend.images.services.ImageValidationService;
import com.ujenzilink.ujenzilink_backend.images.services.R2StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileImageService {

        private final ImageRepository imageRepository;
        private final UserRepository userRepository;
        private final ImageValidationService imageValidationService;
        private final R2StorageService r2StorageService;
        private final SecurityUtil securityUtil;

        @Value("${folders.profile-pictures}")
        private String profilePicturesFolder;

        public ProfileImageService(
                        ImageRepository imageRepository,
                        UserRepository userRepository,
                        ImageValidationService imageValidationService,
                        R2StorageService r2StorageService,
                        SecurityUtil securityUtil) {
                this.imageRepository = imageRepository;
                this.userRepository = userRepository;
                this.imageValidationService = imageValidationService;
                this.r2StorageService = r2StorageService;
                this.securityUtil = securityUtil;
        }

        @Transactional
        public ApiCustomResponse<String> uploadProfilePicture(MultipartFile file) {
                return doUpload(file, "Profile picture uploaded successfully.",
                                "Account not confirmed. Please confirm your account before uploading a profile picture.");
        }

        @Transactional
        public ApiCustomResponse<String> changeProfilePicture(MultipartFile file) {
                return doUpload(file, "Profile picture changed successfully.",
                                "Account not confirmed. Please confirm your account before changing your profile picture.");
        }

        private ApiCustomResponse<String> doUpload(MultipartFile file, String successMessage, String notEnabledMessage) {
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
                                        notEnabledMessage,
                                        HttpStatus.FORBIDDEN.value());
                }

                ImageMetadata metadata = imageValidationService.validateAndExtractMetadata(file);

                String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                String extension = originalName.substring(originalName.lastIndexOf(".") + 1);

                String folder = profilePicturesFolder + "/" + user.getId();
                String fileName = "avatar-" + UUID.randomUUID().toString() + "." + extension;
                R2UploadResponse uploadResponse = r2StorageService.upload(file, folder, fileName);

                Image profileImage = new Image();
                profileImage.setUrl(uploadResponse.key());
                profileImage.setFilename(metadata.filename());
                profileImage.setFileType(metadata.fileType());
                profileImage.setFileSize(metadata.fileSize());
                profileImage.setUser(user);

                // Delete old image from R2 if it exists
                if (user.getProfilePicture() != null) {
                        String oldKey = user.getProfilePicture().getUrl();
                        if (r2StorageService.deleteImageWithVerification(oldKey)) {
                                throw new RuntimeException("Failed to delete old profile picture from R2. Rolling back.");
                        }
                }

                profileImage = imageRepository.save(profileImage);

                user.setProfilePicture(profileImage);
                userRepository.save(user);

                return new ApiCustomResponse<>(
                                uploadResponse.key(),
                                successMessage,
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

                // Delete from R2 storage
                String key = image.getUrl();
                if (r2StorageService.deleteImageWithVerification(key)) {
                        throw new RuntimeException("Failed to delete image from R2. Rolling back.");
                }

                if (user.getProfilePicture() != null && user.getProfilePicture().getId().equals(imageId)) {
                        user.setProfilePicture(null);
                        userRepository.save(user);
                }

                return new ApiCustomResponse<>(
                                null,
                                "Image deleted successfully.",
                                HttpStatus.OK.value());
        }

        public ApiCustomResponse<ProfileImageResponse> getMyProfile() {
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
                        String name = user.getFullName();
                        String profileUrl = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+")
                                        + "&background=random";
                        ProfileImageResponse response = new ProfileImageResponse(
                                        null,
                                        profileUrl,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null);
                        return new ApiCustomResponse<>(
                                        response,
                                        "Default profile picture returned.",
                                        HttpStatus.OK.value());
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
                        String name = targetUser.getFullName();
                        String profileUrl = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+")
                                        + "&background=random";
                        ProfileImageResponse response = new ProfileImageResponse(
                                        null,
                                        profileUrl,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null);
                        return new ApiCustomResponse<>(
                                        response,
                                        "Default profile picture returned.",
                                        HttpStatus.OK.value());
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
