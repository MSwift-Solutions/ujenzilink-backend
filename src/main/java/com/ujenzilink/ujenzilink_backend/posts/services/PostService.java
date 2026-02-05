package com.ujenzilink.ujenzilink_backend.posts.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.CloudinaryUploadResponse;
import com.ujenzilink.ujenzilink_backend.images.dtos.ImageMetadata;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import com.ujenzilink.ujenzilink_backend.images.repositories.ImageRepository;
import com.ujenzilink.ujenzilink_backend.images.services.CloudinaryService;
import com.ujenzilink.ujenzilink_backend.images.services.ImageValidationService;
import com.ujenzilink.ujenzilink_backend.posts.dtos.*;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostImage;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostImageRepository;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostRepository;
import com.ujenzilink.ujenzilink_backend.posts.utils.PostUtils;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageValidationService imageValidationService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private SecurityUtil securityUtil;

    @Transactional(rollbackFor = Exception.class)
    public ApiCustomResponse<CreatePostResponse> createPost(CreatePostRequest request, List<MultipartFile> images) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        boolean hasContent = request.content() != null && !request.content().trim().isEmpty();
        boolean hasImages = images != null && !images.isEmpty();

        if (!hasContent && !hasImages) {
            return new ApiCustomResponse<>(null, "Post must have either content, images, or both.",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (hasImages && images.size() > 10) {
            return new ApiCustomResponse<>(null, "Maximum 10 images allowed per post.", HttpStatus.BAD_REQUEST.value());
        }

        Post post = new Post();
        post.setContent(hasContent ? request.content().trim() : "");
        post.setCreator(user);

        Post savedPost = postRepository.save(post);

        if (hasImages) {
            int order = 0;
            for (MultipartFile file : images) {
                if (file.isEmpty())
                    continue;

                ImageMetadata metadata = imageValidationService.validateAndExtractMetadata(file);
                CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadImage(file, "ujenzilink/post-images");

                Image image = new Image();
                image.setUrl(uploadResponse.secureUrl());
                image.setFilename(metadata.filename());
                image.setFileType(metadata.fileType());
                image.setFileSize(metadata.fileSize());
                image.setWidth(uploadResponse.width());
                image.setHeight(uploadResponse.height());
                image.setUser(user);
                image = imageRepository.save(image);

                PostImage postImage = new PostImage();
                postImage.setPost(savedPost);
                postImage.setImage(image);
                postImage.setImageOrder(order++);
                postImageRepository.save(postImage);
            }
        }

        CreatePostResponse response = new CreatePostResponse(savedPost.getId(), "Post created successfully");
        return new ApiCustomResponse<>(response, "Post created successfully.", HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<PostPageResponse> getAllPosts(String cursor, Integer size) {
        if (size == null || size < 1)
            size = 20;
        if (size > 100)
            size = 100;

        Instant cursorTime = null;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decodedJson = new String(java.util.Base64.getDecoder().decode(cursor));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> cursorData = mapper.readValue(decodedJson, java.util.Map.class);
                String timestamp = (String) cursorData.get("timestamp");
                cursorTime = Instant.parse(timestamp);
            } catch (Exception e) {
                return new ApiCustomResponse<>(null, "Invalid cursor format", HttpStatus.BAD_REQUEST.value());
            }
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, size + 1, sort);

        List<Post> posts;
        if (cursorTime != null) {
            posts = postRepository.findByIsDeletedFalseAndCreatedAtBefore(cursorTime, pageable);
        } else {
            posts = postRepository.findByIsDeletedFalse(pageable);
        }

        boolean hasMore = posts.size() > size;
        if (hasMore)
            posts = posts.subList(0, size);

        List<PostListResponse> postResponses = posts.stream().map(this::mapToPostListResponse).toList();

        String nextCursor = null;
        if (hasMore && !posts.isEmpty()) {
            try {
                Post lastPost = posts.get(posts.size() - 1);
                String cursorJson = String.format("{\"timestamp\":\"%s\"}", lastPost.getCreatedAt().toString());
                nextCursor = java.util.Base64.getEncoder().encodeToString(cursorJson.getBytes());
            } catch (Exception e) {
            }
        }

        PostPageResponse pageResponse = new PostPageResponse(postResponses, nextCursor, hasMore);
        return new ApiCustomResponse<>(pageResponse, "Posts retrieved successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<PostPageResponse> getMyPosts(String cursor, Integer size) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        if (size == null || size < 1)
            size = 20;
        if (size > 100)
            size = 100;

        Instant cursorTime = null;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decodedJson = new String(java.util.Base64.getDecoder().decode(cursor));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> cursorData = mapper.readValue(decodedJson, java.util.Map.class);
                String timestamp = (String) cursorData.get("timestamp");
                cursorTime = Instant.parse(timestamp);
            } catch (Exception e) {
                return new ApiCustomResponse<>(null, "Invalid cursor format", HttpStatus.BAD_REQUEST.value());
            }
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, size + 1, sort);

        List<Post> posts;
        if (cursorTime != null) {
            posts = postRepository.findByCreatorAndIsDeletedFalseAndCreatedAtBefore(currentUser, cursorTime, pageable);
        } else {
            posts = postRepository.findByCreatorAndIsDeletedFalse(currentUser, pageable);
        }

        boolean hasMore = posts.size() > size;
        if (hasMore)
            posts = posts.subList(0, size);

        List<PostListResponse> postResponses = posts.stream().map(this::mapToPostListResponse).toList();

        String nextCursor = null;
        if (hasMore && !posts.isEmpty()) {
            try {
                Post lastPost = posts.get(posts.size() - 1);
                String cursorJson = String.format("{\"timestamp\":\"%s\"}", lastPost.getCreatedAt().toString());
                nextCursor = java.util.Base64.getEncoder().encodeToString(cursorJson.getBytes());
            } catch (Exception e) {
            }
        }

        PostPageResponse pageResponse = new PostPageResponse(postResponses, nextCursor, hasMore);
        return new ApiCustomResponse<>(pageResponse, "My posts retrieved successfully", HttpStatus.OK.value());
    }

    private PostListResponse mapToPostListResponse(Post post) {
        User creator = post.getCreator();
        String creatorName = creator.getFullName();
        String profilePictureUrl = (creator.getProfilePicture() != null)
                ? creator.getProfilePicture().getUrl()
                : "https://ui-avatars.com/api/?name=" + creatorName.replace(" ", "+") + "&background=random";
        String username = (creator.getUserHandle() != null && !creator.getUserHandle().isEmpty())
                ? creator.getUserHandle()
                : creator.getEmail();
        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(creator.getId(), creatorName, username, profilePictureUrl);

        List<String> images = postImageRepository.findByPostOrderByImageOrderAsc(post).stream()
                .map(pi -> pi.getImage().getUrl())
                .toList();

        return new PostListResponse(
                post.getId(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.isEdited(),
                creatorInfo,
                images,
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getBookmarksCount(),
                post.getViews(),
                post.getImpressions());
    }

    public ApiCustomResponse<PostListResponse> getPost(java.util.UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        // Increment impressions when post is fetched
        PostUtils.incrementImpressions(post);
        postRepository.save(post);

        PostListResponse response = mapToPostListResponse(post);
        return new ApiCustomResponse<>(response, "Post retrieved successfully", HttpStatus.OK.value());
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiCustomResponse<Void> editPost(java.util.UUID postId, EditPostRequest request,
            List<MultipartFile> images) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        if (!post.getCreator().getId().equals(currentUser.getId())) {
            return new ApiCustomResponse<>(null, "You do not have permission to edit this post.",
                    HttpStatus.FORBIDDEN.value());
        }

        boolean contentChanged = false;

        // Update content if provided
        if (request.content() != null) {
            String newContent = request.content().trim();
            if (!newContent.equals(post.getContent())) {
                post.setContent(newContent);
                contentChanged = true;
            }
        }

        // Handle removed images
        if (request.removedImageIds() != null && !request.removedImageIds().isEmpty()) {
            for (java.util.UUID imageId : request.removedImageIds()) {
                Image image = imageRepository.findById(imageId).orElse(null);
                if (image != null && !image.getIsDeleted()) {
                    image.setIsDeleted(true);
                    imageRepository.save(image);
                    contentChanged = true;
                }
            }
        }

        // Handle new images
        if (images != null && !images.isEmpty()) {
            if (images.size() > 10) {
                return new ApiCustomResponse<>(null, "Maximum 10 images allowed per post.",
                        HttpStatus.BAD_REQUEST.value());
            }

            // Get current max order
            List<PostImage> existingImages = postImageRepository.findByPostOrderByImageOrderAsc(post);
            int maxOrder = existingImages.stream()
                    .mapToInt(PostImage::getImageOrder)
                    .max()
                    .orElse(-1);

            for (MultipartFile file : images) {
                if (file.isEmpty())
                    continue;

                ImageMetadata metadata = imageValidationService.validateAndExtractMetadata(file);
                CloudinaryUploadResponse uploadResponse = cloudinaryService.uploadImage(file, "ujenzilink/post-images");

                Image image = new Image();
                image.setUrl(uploadResponse.secureUrl());
                image.setFilename(metadata.filename());
                image.setFileType(metadata.fileType());
                image.setFileSize(metadata.fileSize());
                image.setWidth(uploadResponse.width());
                image.setHeight(uploadResponse.height());
                image.setUser(currentUser);
                image = imageRepository.save(image);

                PostImage postImage = new PostImage();
                postImage.setPost(post);
                postImage.setImage(image);
                postImage.setImageOrder(++maxOrder);
                postImageRepository.save(postImage);
                contentChanged = true;
            }
        }

        if (contentChanged) {
            post.setEdited(true);
        }

        postRepository.save(post);
        return new ApiCustomResponse<>(null, "Post updated successfully", HttpStatus.OK.value());
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiCustomResponse<Void> deletePost(java.util.UUID postId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        if (!post.getCreator().getId().equals(currentUser.getId())) {
            return new ApiCustomResponse<>(null, "You do not have permission to delete this post.",
                    HttpStatus.FORBIDDEN.value());
        }

        post.setDeleted(true);
        postRepository.save(post);

        return new ApiCustomResponse<>(null, "Post deleted successfully", HttpStatus.OK.value());
    }
}
