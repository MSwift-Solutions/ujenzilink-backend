package com.ujenzilink.ujenzilink_backend.auth.admin.services;

import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminPostPageResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.AdminPostResponse;
import com.ujenzilink.ujenzilink_backend.auth.admin.dtos.DeletePostAdminRequest;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.services.NotificationService;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostImage;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostImageRepository;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostRepository;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminPostManagementService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public ApiCustomResponse<AdminPostPageResponse> getPostsByUserId(UUID userId, Integer limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiCustomResponse<>(null, "User not found", HttpStatus.NOT_FOUND.value());
        }

        int pageSize = (limit != null && limit > 0) ? limit : 20;
        Pageable pageable = PageRequest.of(0, pageSize);
        List<Post> posts = postRepository.findByCreatorAndIsDeletedFalse(user, pageable);

        List<AdminPostResponse> responses = posts.stream().map(this::mapToAdminPostResponse).collect(Collectors.toList());

        AdminPostPageResponse pageResponse = new AdminPostPageResponse(responses, null, posts.size() >= pageSize);
        return new ApiCustomResponse<>(pageResponse, "User posts retrieved successfully", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<Void> deletePostByAdmin(UUID postId, DeletePostAdminRequest request) {
        Optional<User> adminOpt = securityUtil.getAuthenticatedUser();
        if (adminOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }
        User currentAdmin = adminOpt.get();

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found or already deleted", HttpStatus.NOT_FOUND.value());
        }

        post.setDeleted(true);
        post.setAdminDeletionReason(request.reason());
        post.setAdminDeletedBy(currentAdmin);
        postRepository.save(post);

        // Notify the creator
        String contentSnippet = post.getContent();
        if (contentSnippet.length() > 50) {
            contentSnippet = contentSnippet.substring(0, 47) + "...";
        }

        notificationService.createNotification(
                post.getCreator(),
                currentAdmin,
                NotificationType.SYSTEM_ANNOUNCEMENT,
                "Post Removed",
                "Your post (\"" + contentSnippet + "\") has been removed by an admin. Reason: " + request.reason(),
                NotificationPriority.HIGH,
                false,
                null,
                null
        );

        return new ApiCustomResponse<>(null, "Post deleted successfully", HttpStatus.OK.value());
    }

    private AdminPostResponse mapToAdminPostResponse(Post post) {
        User creator = post.getCreator();
        String creatorName = (creator != null) ? creator.getFullName() : "Unknown";
        String profilePictureUrl = (creator != null && creator.getProfilePicture() != null) 
                ? creator.getProfilePicture().getUrl() 
                : null;
        String username = (creator != null) 
                ? ((creator.getUserHandle() != null && !creator.getUserHandle().isEmpty()) ? creator.getUserHandle() : creator.getEmail())
                : "unknown";
        
        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(creator != null ? creator.getId() : null, creatorName, username, profilePictureUrl);

        List<String> images = postImageRepository.findByPostOrderByImageOrderAsc(post)
                .stream().map(image -> image.getImage().getUrl()).collect(Collectors.toList());

        return new AdminPostResponse(
                post.getId(),
                post.getContent(),
                post.getCreatedAt(),
                creatorInfo,
                images,
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getViews(),
                post.getImpressions(),
                post.isDeleted(),
                post.getAdminDeletionReason(),
                post.getAdminDeletedBy() != null ? post.getAdminDeletedBy().getUserHandle() : null
        );
    }
}
