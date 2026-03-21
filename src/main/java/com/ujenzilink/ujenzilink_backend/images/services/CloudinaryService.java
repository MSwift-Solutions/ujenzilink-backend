package com.ujenzilink.ujenzilink_backend.images.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryUploadResponse;
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

    public CloudinaryUploadResponse uploadImage(MultipartFile file,
                                                String folder) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false,
                            // Transformation: auto-format, auto-quality, and max width of 2000px
                            "transformation", new Transformation<>()
                                    .fetchFormat("auto")
                                    .quality("auto")
                                    .width(2000)
                                    .crop("limit")));

            return com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryUploadResponse.from(uploadResult);
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage());
        }
    }

    // Default upload to profile-pictures for backward compatibility
    public com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryUploadResponse uploadImage(MultipartFile file) {
        return uploadImage(file, "ujenzilink/profile-pictures");
    }
}
