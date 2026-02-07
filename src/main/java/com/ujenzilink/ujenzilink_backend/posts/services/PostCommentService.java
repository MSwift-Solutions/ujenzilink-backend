package com.ujenzilink.ujenzilink_backend.posts.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostComment;
import com.ujenzilink.ujenzilink_backend.posts.models.PostCommentLike;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostCommentLikeRepository;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostCommentRepository;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostRepository;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CommentDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateCommentRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ReplyDTO;
import com.ujenzilink.ujenzilink_backend.user_mgt.enums.ActivityType;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostCommentService {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentLikeRepository postCommentLikeRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ActivityService activityService;

    public ApiCustomResponse<List<CommentDTO>> getPostComments(UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        User currentUser = securityUtil.getAuthenticatedUser().orElse(null);

        // Fetch all non-deleted comments for this post
        List<PostComment> allComments = postCommentRepository
                .findByPostAndIsDeletedFalseOrderByCreatedAtAsc(post);

        // Map to hold children for each parent
        Map<UUID, List<PostComment>> parentToChildren = new HashMap<>();
        List<PostComment> rootComments = new ArrayList<>();

        for (PostComment comment : allComments) {
            if (comment.getParentComment() == null) {
                rootComments.add(comment);
            } else {
                parentToChildren.computeIfAbsent(comment.getParentComment().getId(), k -> new ArrayList<>())
                        .add(comment);
            }
        }

        User finalCurrentUser = currentUser;
        List<CommentDTO> commentDTOs = rootComments.stream().map(comment -> {
            List<ReplyDTO> flattenedReplies = new ArrayList<>();
            collectDescendants(comment.getId(), parentToChildren, flattenedReplies, finalCurrentUser);

            return mapToCommentDTO(comment, flattenedReplies, finalCurrentUser);
        }).collect(Collectors.toList());

        return new ApiCustomResponse<>(commentDTOs, "Comments retrieved successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<CommentDTO> createComment(UUID postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "You must be logged in to comment", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setCommenter(currentUser);
        comment.setContent(request.text());

        if (request.parentId() != null) {
            PostComment parent = postCommentRepository.findById(request.parentId()).orElse(null);
            if (parent == null || parent.isDeleted()) {
                return new ApiCustomResponse<>(null, "Parent comment not found", HttpStatus.NOT_FOUND.value());
            }
            comment.setParentComment(parent);
        }

        PostComment savedComment = postCommentRepository.save(comment);

        // Update post comments count
        post.setCommentsCount((post.getCommentsCount() != null ? post.getCommentsCount() : 0) + 1);
        postRepository.save(post);

        // Log comment creation activity
        activityService.logActivity(currentUser, ActivityType.CREATE_COMMENT, savedComment.getId());

        // Map to DTO for response (newly created comment has no replies)
        CommentDTO responseDTO = mapToCommentDTO(savedComment, new ArrayList<>(), currentUser);

        return new ApiCustomResponse<>(responseDTO, "Comment created successfully", HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<String> likeComment(UUID commentId) {
        PostComment comment = postCommentRepository.findById(commentId).orElse(null);
        if (comment == null || comment.isDeleted()) {
            return new ApiCustomResponse<>(null, "Comment not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "You must be logged in to like a comment",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        Optional<PostCommentLike> existingLike = postCommentLikeRepository.findByCommentAndUser(comment,
                currentUser);

        if (existingLike.isPresent()) {
            postCommentLikeRepository.delete(existingLike.get());

            // Update counts
            comment.setLikesCount((comment.getLikesCount() > 0 ? comment.getLikesCount() : 1) - 1);
            postCommentRepository.save(comment);

            activityService.logActivity(currentUser, ActivityType.UNLIKE_COMMENT, commentId);
            return new ApiCustomResponse<>(null, "Comment unliked successfully", HttpStatus.OK.value());
        } else {
            PostCommentLike commentLike = new PostCommentLike(comment, currentUser);
            postCommentLikeRepository.save(commentLike);

            // Update counts
            comment.setLikesCount((comment.getLikesCount() != null ? comment.getLikesCount() : 0) + 1);
            postCommentRepository.save(comment);

            activityService.logActivity(currentUser, ActivityType.LIKE_COMMENT, commentId);
            return new ApiCustomResponse<>(null, "Comment liked successfully", HttpStatus.CREATED.value());
        }
    }

    private void collectDescendants(UUID parentId, Map<UUID, List<PostComment>> parentToChildren,
            List<ReplyDTO> flattenedReplies, User currentUser) {
        List<PostComment> children = parentToChildren.get(parentId);
        if (children != null) {
            for (PostComment child : children) {
                flattenedReplies.add(mapToReplyDTO(child, currentUser));
                // Recurse to find all descendants
                collectDescendants(child.getId(), parentToChildren, flattenedReplies, currentUser);
            }
        }
    }

    private CommentDTO mapToCommentDTO(PostComment comment, List<ReplyDTO> replies, User currentUser) {
        CreatorInfoDTO commenterInfo = mapToCreatorInfoDTO(comment.getCommenter());
        boolean hasLiked = currentUser != null
                && postCommentLikeRepository.existsByCommentAndUser(comment, currentUser);
        int likesCount = (int) postCommentLikeRepository.countByComment(comment);

        return new CommentDTO(
                comment.getId(),
                commenterInfo,
                comment.getContent(),
                comment.getCreatedAt(),
                hasLiked,
                likesCount,
                replies);
    }

    private ReplyDTO mapToReplyDTO(PostComment comment, User currentUser) {
        CreatorInfoDTO commenterInfo = mapToCreatorInfoDTO(comment.getCommenter());
        boolean hasLiked = currentUser != null
                && postCommentLikeRepository.existsByCommentAndUser(comment, currentUser);
        int likesCount = (int) postCommentLikeRepository.countByComment(comment);

        return new ReplyDTO(
                comment.getId(),
                commenterInfo,
                comment.getContent(),
                comment.getCreatedAt(),
                hasLiked,
                likesCount);
    }

    private CreatorInfoDTO mapToCreatorInfoDTO(User user) {
        String name = user.getFullName();
        String profilePictureUrl = (user.getProfilePicture() != null)
                ? user.getProfilePicture().getUrl()
                : "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random";
        String username = (user.getUserHandle() != null && !user.getUserHandle().isEmpty())
                ? user.getUserHandle()
                : user.getEmail();

        return new CreatorInfoDTO(user.getId(), name, username, profilePictureUrl);
    }
}
