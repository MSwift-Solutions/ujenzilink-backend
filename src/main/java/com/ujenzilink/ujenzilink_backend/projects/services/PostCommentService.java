package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CommentDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ReplyDTO;
import com.ujenzilink.ujenzilink_backend.projects.models.PostComment;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import com.ujenzilink.ujenzilink_backend.projects.repositories.CommentLikeRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.PostCommentRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectStageRepository;
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
    private ProjectStageRepository projectStageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    public ApiCustomResponse<List<CommentDTO>> getStageComments(UUID stageId) {
        ProjectStage stage = projectStageRepository.findById(stageId).orElse(null);
        if (stage == null || stage.isDeleted()) {
            return new ApiCustomResponse<>(null, "Post not found", HttpStatus.NOT_FOUND.value());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            currentUser = userRepository.findFirstByEmail(authentication.getName());
        }

        // Fetch all non-deleted comments for this stage
        List<PostComment> allComments = postCommentRepository.findByStageAndIsDeletedFalseOrderByCreatedAtAsc(stage);

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
