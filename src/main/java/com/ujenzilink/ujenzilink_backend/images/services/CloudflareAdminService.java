package com.ujenzilink.ujenzilink_backend.images.services;

import com.ujenzilink.ujenzilink_backend.configs.R2StorageProperties;
import com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryResourceDTO;
import com.ujenzilink.ujenzilink_backend.images.dtos.HangingResourcesResponse;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostImageRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.StagePhotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CloudflareAdminService {

    private final ImageRepository imageRepository;
    private final PostImageRepository postImageRepository;
    private final StagePhotoRepository stagePhotoRepository;
    private final S3Client s3Client;
    private final R2StorageProperties r2Props;

    @Value("${folders.profile-pictures}")
    private String profilePicturesFolder;

    @Value("${folders.post-images}")
    private String postImagesFolder;

    @Value("${folders.project-stage-images}")
    private String projectStageImagesFolder;

    public CloudflareAdminService(ImageRepository imageRepository,
                                  PostImageRepository postImageRepository,
                                  StagePhotoRepository stagePhotoRepository,
                                  S3Client s3Client,
                                  R2StorageProperties r2Props) {
        this.imageRepository = imageRepository;
        this.postImageRepository = postImageRepository;
        this.stagePhotoRepository = stagePhotoRepository;
        this.s3Client = s3Client;
        this.r2Props = r2Props;
    }

    // --- User Profile Pictures (R2) ---
    public HangingResourcesResponse getOrphanedProfilePictures() {
        return getHangingResourcesWithTypeR2(profilePicturesFolder, "USERS", "ORPHANED");
    }

    public HangingResourcesResponse getFlaggedProfilePictures() {
        return getHangingResourcesWithTypeR2(profilePicturesFolder, "USERS", "FLAGGED");
    }

    public HangingResourcesResponse getParentDeletedProfilePictures() {
        return getHangingResourcesWithTypeR2(profilePicturesFolder, "USERS", "PARENT_DELETED");
    }

    // --- Post Images (R2) ---
    public HangingResourcesResponse getOrphanedPostImages() {
        return getHangingResourcesWithTypeR2(postImagesFolder, "POSTS", "ORPHANED");
    }

    public HangingResourcesResponse getFlaggedPostImages() {
        return getHangingResourcesWithTypeR2(postImagesFolder, "POSTS", "FLAGGED");
    }

    public HangingResourcesResponse getParentDeletedPostImages() {
        return getHangingResourcesWithTypeR2(postImagesFolder, "POSTS", "PARENT_DELETED");
    }

    // --- Project Stage Images (R2) ---
    public HangingResourcesResponse getOrphanedProjectImages() {
        return getHangingResourcesWithTypeR2(projectStageImagesFolder, "PROJECTS", "ORPHANED");
    }

    public HangingResourcesResponse getFlaggedProjectImages() {
        return getHangingResourcesWithTypeR2(projectStageImagesFolder, "PROJECTS", "FLAGGED");
    }

    public HangingResourcesResponse getParentDeletedProjectImages() {
        return getHangingResourcesWithTypeR2(projectStageImagesFolder, "PROJECTS", "PARENT_DELETED");
    }

    private HangingResourcesResponse getHangingResourcesWithTypeR2(String folderPrefix, String category, String reasonType) {
        try {
            List<S3Object> r2Objects = fetchAllR2Objects(folderPrefix);
            List<CloudinaryResourceDTO> hangingResources = new ArrayList<>();
            long totalSize = 0;

            for (S3Object s3Object : r2Objects) {
                String key = s3Object.key();
                String url = r2Props.publicUrl() + "/" + key;
                String format = key.contains(".") ? key.substring(key.lastIndexOf(".") + 1) : "unknown";
                long size = s3Object.size();
                Instant createdAt = s3Object.lastModified();

                Optional<Image> imageOpt = imageRepository.findByUrl(key);

                boolean isHanging = false;
                String currentReason = "";

                if (imageOpt.isEmpty()) {
                    if (reasonType.equalsIgnoreCase("ORPHANED")) {
                        isHanging = true;
                        currentReason = "ORPHANED: No database record found in R2.";
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
                    totalSize += size;
                    hangingResources.add(new CloudinaryResourceDTO(
                            key,
                            url,
                            format,
                            size,
                            createdAt,
                            currentReason));
                }
            }

            return new HangingResourcesResponse(
                    hangingResources,
                    hangingResources.size(),
                    totalSize);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch R2 categorized hanging resources: " + e.getMessage(), e);
        }
    }

    private List<S3Object> fetchAllR2Objects(String prefix) {
        List<S3Object> allObjects = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                    .bucket(r2Props.bucketName())
                    .prefix(prefix);

            if (continuationToken != null) {
                builder.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(builder.build());
            allObjects.addAll(response.contents());
            continuationToken = response.nextContinuationToken();
        } while (continuationToken != null);

        return allObjects;
    }

    @Transactional
    public Map<String, String> deleteResources(List<String> publicIds) {
        java.util.Map<String, String> results = new java.util.HashMap<>();
        for (String publicId : publicIds) {
            try {
                // 1. Delete from R2 storage ONLY
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(r2Props.bucketName())
                        .key(publicId)
                        .build());
                results.put(publicId, "DELETED: File removed from R2.");
            } catch (Exception e) {
                results.put(publicId, "ERROR: " + e.getMessage());
            }
        }
        return results;
    }
}
