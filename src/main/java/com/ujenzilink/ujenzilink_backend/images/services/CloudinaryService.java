package com.ujenzilink.ujenzilink_backend.images.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ujenzilink.ujenzilink_backend.images.exceptions.CloudinaryUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads an image file to Cloudinary and returns the secure URL.
     * 
     * @param file The multipart file to upload
     * @return The secure HTTPS URL of the uploaded image
     * @throws CloudinaryUploadException if upload fails
     */
    public String uploadImage(MultipartFile file) {
        try {
            // Upload to Cloudinary with specific options
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "ujenzilink/profile-pictures",
                    "resource_type", "image",
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    uploadParams);

            // Extract and return the secure URL
            String secureUrl = (String) uploadResult.get("secure_url");

            if (secureUrl == null || secureUrl.isEmpty()) {
                throw new CloudinaryUploadException("Failed to retrieve secure URL from Cloudinary response.");
            }

            return secureUrl;

        } catch (IOException e) {
            throw new CloudinaryUploadException(
                    "Failed to upload image to Cloudinary. Please try again later.",
                    e);
        } catch (Exception e) {
            throw new CloudinaryUploadException(
                    "An unexpected error occurred during image upload: " + e.getMessage(),
                    e);
        }
    }
}
