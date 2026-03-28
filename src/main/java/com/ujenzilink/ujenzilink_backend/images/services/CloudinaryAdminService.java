package com.ujenzilink.ujenzilink_backend.images.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryResourceDTO;
import com.ujenzilink.ujenzilink_backend.images.dtos.HangingResourcesResponse;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CloudinaryAdminService {

    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;
    private final com.ujenzilink.ujenzilink_backend.posts.repositories.PostImageRepository postImageRepository;
    private final com.ujenzilink.ujenzilink_backend.projects.repositories.StagePhotoRepository stagePhotoRepository;

    @Value("${cloudinary.root-folder:ujenzilink}")
    private String cloudinaryRoot;

    @Value("${folders.profile-pictures}")
    private String profilePicturesFolder;

    @Value("${folders.post-images}")
    private String postImagesFolder;

    @Value("${folders.project-stage-images}")
    private String projectStageImagesFolder;

    public CloudinaryAdminService(Cloudinary cloudinary,
            ImageRepository imageRepository,
            com.ujenzilink.ujenzilink_backend.posts.repositories.PostImageRepository postImageRepository,
            com.ujenzilink.ujenzilink_backend.projects.repositories.StagePhotoRepository stagePhotoRepository) {
        this.cloudinary = cloudinary;
        this.imageRepository = imageRepository;
        this.postImageRepository = postImageRepository;
        this.stagePhotoRepository = stagePhotoRepository;
    }

    // --- User Profile Pictures ---
    public HangingResourcesResponse getOrphanedProfilePictures() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + profilePicturesFolder, "USERS", "ORPHANED");
    }

    public HangingResourcesResponse getFlaggedProfilePictures() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + profilePicturesFolder, "USERS", "FLAGGED");
    }

    public HangingResourcesResponse getParentDeletedProfilePictures() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + profilePicturesFolder, "USERS", "PARENT_DELETED");
    }

    // --- Post Images ---
    public HangingResourcesResponse getOrphanedPostImages() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + postImagesFolder, "POSTS", "ORPHANED");
    }

    public HangingResourcesResponse getFlaggedPostImages() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + postImagesFolder, "POSTS", "FLAGGED");
    }

    public HangingResourcesResponse getParentDeletedPostImages() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + postImagesFolder, "POSTS", "PARENT_DELETED");
    }

    // --- Project Stage Images ---
    public HangingResourcesResponse getOrphanedProjectImages() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + projectStageImagesFolder, "PROJECTS", "ORPHANED");
    }

    public HangingResourcesResponse getFlaggedProjectImages() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + projectStageImagesFolder, "PROJECTS", "FLAGGED");
    }

    public HangingResourcesResponse getParentDeletedProjectImages() {
        return getHangingResourcesWithType(cloudinaryRoot + "/" + projectStageImagesFolder, "PROJECTS", "PARENT_DELETED");
    }

    @Deprecated
    public HangingResourcesResponse getHangingProfilePictures() {
        return getOrphanedProfilePictures();
    }

    @Deprecated
    public HangingResourcesResponse getHangingPostImages() {
        return getOrphanedPostImages();
    }

    @Deprecated
    public HangingResourcesResponse getHangingProjectStageImages() {
        return getOrphanedProjectImages();
    }

    private HangingResourcesResponse getHangingResourcesWithType(String folderPrefix, String category, String reasonType) {
        try {
            List<Map<String, Object>> cloudinaryResources = fetchAllCloudinaryResources(folderPrefix);
            List<CloudinaryResourceDTO> hangingResources = new ArrayList<>();
            long totalSize = 0;

            for (Map<String, Object> resource : cloudinaryResources) {
                String secureUrl = (String) resource.get("secure_url");
                String publicId = (String) resource.get("public_id");
                String format = (String) resource.get("format");
                Long bytes = ((Number) resource.get("bytes")).longValue();
                Instant createdAt = Instant.parse((String) resource.get("created_at"));

                Optional<Image> imageOpt = imageRepository.findByUrl(publicId);

                boolean isHanging = false;
                String currentReason = "";

                if (imageOpt.isEmpty()) {
                    if (reasonType.equalsIgnoreCase("ORPHANED")) {
                        isHanging = true;
                        currentReason = "ORPHANED: No database record found.";
                    }
                } else {
                    Image image = imageOpt.get();
                    if (image.getIsDeleted()) {
                        if (reasonType.equalsIgnoreCase("FLAGGED")) {
                            isHanging = true;
                            currentReason = "FLAGGED: Exists in database but marked as deleted.";
                        }
                    } else if (reasonType.equalsIgnoreCase("PARENT_DELETED")) {
                        if (category.equals("USERS")) {
                            if (image.getUser() != null && image.getUser().getIsDeleted()) {
                                isHanging = true;
                                currentReason = "PARENT_DELETED: Profile picture owner is deleted.";
                            }
                        } else if (category.equals("POSTS")) {
                            isHanging = postImageRepository.findFirstByImage(image)
                                    .map(pi -> pi.getPost().isDeleted())
                                    .orElse(true);
                            if (isHanging)
                                currentReason = "PARENT_DELETED: Associated post is deleted or missing.";
                        } else if (category.equals("PROJECTS")) {
                            isHanging = stagePhotoRepository.findFirstByImage(image)
                                    .map(sp -> sp.getStage() != null && sp.getStage().getProject().isDeleted())
                                    .orElse(true);
                            if (isHanging)
                                currentReason = "PARENT_DELETED: Associated project is deleted or missing.";
                        }
                    }
                }

                if (isHanging) {
                    totalSize += bytes;
                    hangingResources.add(new CloudinaryResourceDTO(
                            publicId,
                            secureUrl,
                            format,
                            bytes,
                            createdAt,
                            currentReason));
                }
            }

            return new HangingResourcesResponse(
                    hangingResources,
                    hangingResources.size(),
                    totalSize);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch categorized hanging resources: " + e.getMessage(), e);
        }
    }


    private List<Map<String, Object>> fetchAllCloudinaryResources(String folderPrefix) throws Exception {
        List<Map<String, Object>> allResources = new ArrayList<>();
        String nextCursor = null;

        do {
            Map<String, Object> options = ObjectUtils.asMap(
                    "type", "upload",
                    "prefix", folderPrefix,
                    "max_results", 500
            );
            if (nextCursor != null) {
                options.put("next_cursor", nextCursor);
            }

            ApiResponse response = cloudinary.api().resources(options);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resources = (List<Map<String, Object>>) response.get("resources");
            if (resources != null) {
                allResources.addAll(resources);
            }

            nextCursor = (String) response.get("next_cursor");
        } while (nextCursor != null);

        return allResources;
    }
}
