package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CommentDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateCommentRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ReplyDTO;
import com.ujenzilink.ujenzilink_backend.projects.models.CommentLike;
import com.ujenzilink.ujenzilink_backend.projects.models.PostComment;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.repositories.CommentLikeRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.PostCommentRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostCommentService {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    public ApiCustomResponse<List<CommentDTO>> getProjectComments(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            currentUser = userRepository.findFirstByEmail(authentication.getName());
        }

        // Fetch all non-deleted comments for this project
        List<PostComment> allComments = postCommentRepository
                .findByProjectAndIsDeletedFalseOrderByCreatedAtAsc(project);

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

    public ApiCustomResponse<CommentDTO> createComment(UUID projectId, CreateCommentRequest request) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return new ApiCustomResponse<>(null, "You must be logged in to comment", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userRepository.findFirstByEmail(authentication.getName());
        if (currentUser == null) {
            return new ApiCustomResponse<>(null, "User not found", HttpStatus.NOT_FOUND.value());
        }

        PostComment comment = new PostComment();
        comment.setProject(project);
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

        // Map to DTO for response (newly created comment has no replies)
        CommentDTO responseDTO = mapToCommentDTO(savedComment, new ArrayList<>(), currentUser);

        return new ApiCustomResponse<>(responseDTO, "Comment created successfully", HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<String> likeComment(UUID commentId) {
        PostComment comment = postCommentRepository.findById(commentId).orElse(null);
        if (comment == null || comment.isDeleted()) {
            return new ApiCustomResponse<>(null, "Comment not found", HttpStatus.NOT_FOUND.value());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return new ApiCustomResponse<>(null, "You must be logged in to like a comment",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userRepository.findFirstByEmail(authentication.getName());
        if (currentUser == null) {
            return new ApiCustomResponse<>(null, "User not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentAndUser(comment, currentUser);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            return new ApiCustomResponse<>(null, "Comment unliked successfully", HttpStatus.OK.value());
        } else {
            CommentLike commentLike = new CommentLike(comment, currentUser);
            commentLikeRepository.save(commentLike);
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
        boolean hasLiked = currentUser != null && commentLikeRepository.existsByCommentAndUser(comment, currentUser);
        int likesCount = (int) commentLikeRepository.countByComment(comment);

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
        boolean hasLiked = currentUser != null && commentLikeRepository.existsByCommentAndUser(comment, currentUser);
        int likesCount = (int) commentLikeRepository.countByComment(comment);

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
