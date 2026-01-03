package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ApiCustomResponse<String> uploadProfilePicture(String profilePictureUrl) {
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

        // Check if account is confirmed/enabled
        if (!user.getIsEnabled()) {
            return new ApiCustomResponse<>(
                    null,
                    "Account not confirmed. Please confirm your account before uploading a profile picture.",
                    HttpStatus.FORBIDDEN.value());
        }

        // Create or update profile picture
        Image profileImage;
        if (user.getProfilePicture() != null) {
            // Update existing profile picture
            profileImage = user.getProfilePicture();
            profileImage.setUrl(profilePictureUrl.trim());
            profileImage = imageRepository.save(profileImage);
        } else {
            // Create new profile picture
            profileImage = new Image();
            profileImage.setUrl(profilePictureUrl.trim());
            profileImage.setUser(user);
            profileImage = imageRepository.save(profileImage);
            user.setProfilePicture(profileImage);
        }

        userRepository.save(user);

        return new ApiCustomResponse<>(
                null,
                "Profile picture uploaded successfully.",
                HttpStatus.OK.value());
    }
}

