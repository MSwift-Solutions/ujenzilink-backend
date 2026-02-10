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
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectLikeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ujenzilink.ujenzilink_backend.notifications.services.NotificationService;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;

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

    @Autowired
    private com.ujenzilink.ujenzilink_backend.posts.repositories.PostBookmarkRepository postBookmarkRepository;

    @Autowired
    private com.ujenzilink.ujenzilink_backend.posts.repositories.PostLikeRepository postLikeRepository;

    @Autowired
    private NotificationService notificationService;

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

        // Send in-app notification
        notificationService.createNotification(
                user,
                null,
                NotificationType.POST_CREATED,
                "Post Created",
                "Your post has been shared successfully.",
                NotificationPriority.LOW,
                false,
                null,
                null);

        return new ApiCustomResponse<>(response, "Post created successfully.", HttpStatus.CREATED.value());
    }

    @Transactional
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

        // Increment impressions in bulk
        if (!posts.isEmpty()) {
            java.util.List<java.util.UUID> postIds = posts.stream().map(Post::getId).toList();
            postRepository.incrementImpressionsInBulk(postIds);
        }

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

    @Transactional
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

        // Increment impressions in bulk
        if (!posts.isEmpty()) {
            java.util.List<java.util.UUID> postIds = posts.stream().map(Post::getId).toList();
            postRepository.incrementImpressionsInBulk(postIds);
        }

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

    public ApiCustomResponse<PostListResponse> getEditablePostData(java.util.UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

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

    @Transactional(rollbackFor = Exception.class)
    public ApiCustomResponse<String> toggleBookmark(java.util.UUID postId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<com.ujenzilink.ujenzilink_backend.posts.models.PostBookmark> existingBookmark = postBookmarkRepository
                .findByPostAndUserAndIsDeletedFalse(post, user);

        if (existingBookmark.isPresent()) {
            // Unbookmark
            com.ujenzilink.ujenzilink_backend.posts.models.PostBookmark bookmark = existingBookmark.get();
            bookmark.setDeleted(true);
            postBookmarkRepository.save(bookmark);

            // Decrement bookmarks count
            Integer currentCount = post.getBookmarksCount();
            if (currentCount != null && currentCount > 0) {
                post.setBookmarksCount(currentCount - 1);
                postRepository.save(post);
            }

            return new ApiCustomResponse<>("Unbookmarked", "Post unbookmarked successfully", HttpStatus.OK.value());
        } else {
            // Bookmark
            com.ujenzilink.ujenzilink_backend.posts.models.PostBookmark newBookmark = new com.ujenzilink.ujenzilink_backend.posts.models.PostBookmark(
                    post, user);
            postBookmarkRepository.save(newBookmark);

            // Increment bookmarks count
            Integer currentCount = post.getBookmarksCount();
            post.setBookmarksCount(currentCount != null ? currentCount + 1 : 1);
            postRepository.save(post);

            return new ApiCustomResponse<>("Bookmarked", "Post bookmarked successfully", HttpStatus.CREATED.value());
        }
    }

    public ApiCustomResponse<Boolean> checkBookmarkStatus(java.util.UUID postId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(false, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(false, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        boolean isBookmarked = postBookmarkRepository.existsByPostAndUserAndIsDeletedFalse(post, user);

        return new ApiCustomResponse<>(isBookmarked, "Bookmark status checked successfully", HttpStatus.OK.value());
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiCustomResponse<String> toggleLike(java.util.UUID postId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<com.ujenzilink.ujenzilink_backend.posts.models.PostLike> existingLike = postLikeRepository
                .findByPostAndUserAndIsDeletedFalse(post, user);

        if (existingLike.isPresent()) {
            // Unlike
            com.ujenzilink.ujenzilink_backend.posts.models.PostLike like = existingLike.get();
            like.setDeleted(true);
            postLikeRepository.save(like);

            // Decrement likes count
            Integer currentCount = post.getLikesCount();
            if (currentCount != null && currentCount > 0) {
                post.setLikesCount(currentCount - 1);
                postRepository.save(post);
            }

            return new ApiCustomResponse<>("Unliked", "Post unliked successfully", HttpStatus.OK.value());
        } else {
            // Like
            com.ujenzilink.ujenzilink_backend.posts.models.PostLike newLike = new com.ujenzilink.ujenzilink_backend.posts.models.PostLike(
                    post, user);
            postLikeRepository.save(newLike);

            // Increment likes count
            Integer currentCount = post.getLikesCount();
            post.setLikesCount(currentCount != null ? currentCount + 1 : 1);
            postRepository.save(post);

            return new ApiCustomResponse<>("Liked", "Post liked successfully", HttpStatus.CREATED.value());
        }
    }

    public ApiCustomResponse<Boolean> checkLikeStatus(java.util.UUID postId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(false, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(false, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        boolean isLiked = postLikeRepository.existsByPostAndUserAndIsDeletedFalse(post, user);

        return new ApiCustomResponse<>(isLiked, "Like status checked successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<List<ProjectLikeDTO>> getPostLikes(java.util.UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        List<com.ujenzilink.ujenzilink_backend.posts.models.PostLike> likes = postLikeRepository
                .findByPostAndIsDeletedFalse(post);

        List<ProjectLikeDTO> dtos = likes.stream().map(like -> {
            User liker = like.getUser();
            String likerName = liker.getFullName();
            String profilePictureUrl = (liker.getProfilePicture() != null)
                    ? liker.getProfilePicture().getUrl()
                    : "https://ui-avatars.com/api/?name=" + likerName.replace(" ", "+")
                            + "&background=random";
            String username = (liker.getUserHandle() != null && !liker.getUserHandle().isEmpty())
                    ? liker.getUserHandle()
                    : liker.getEmail();

            CreatorInfoDTO userInfo = new CreatorInfoDTO(liker.getId(), likerName, username, profilePictureUrl);
            return new ProjectLikeDTO(userInfo, like.getCreatedAt());
        }).toList();

        return new ApiCustomResponse<>(dtos, "Post likes retrieved successfully", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<PostPageResponse> getBookmarkedPosts(String cursor, Integer size) {
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

        // Get all bookmarks for this user
        List<com.ujenzilink.ujenzilink_backend.posts.models.PostBookmark> bookmarks = postBookmarkRepository
                .findByUserAndIsDeletedFalseOrderByCreatedAtDesc(currentUser);

        // Extract posts and apply cursor filter
        Instant finalCursorTime = cursorTime;
        List<Post> posts = bookmarks.stream()
                .map(com.ujenzilink.ujenzilink_backend.posts.models.PostBookmark::getPost)
                .filter(post -> !post.isDeleted())
                .filter(post -> finalCursorTime == null || post.getCreatedAt().isBefore(finalCursorTime))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(size + 1)
                .toList();

        boolean hasMore = posts.size() > size;
        if (hasMore)
            posts = posts.subList(0, size);

        // Increment impressions in bulk
        if (!posts.isEmpty()) {
            java.util.List<java.util.UUID> postIds = posts.stream().map(Post::getId).toList();
            postRepository.incrementImpressionsInBulk(postIds);
        }

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
        return new ApiCustomResponse<>(pageResponse, "Bookmarked posts retrieved successfully", HttpStatus.OK.value());
    }
}
