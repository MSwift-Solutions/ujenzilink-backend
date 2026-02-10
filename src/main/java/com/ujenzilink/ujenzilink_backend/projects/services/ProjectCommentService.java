package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CommentDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateCommentRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ReplyDTO;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectCommentLike;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectComment;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectCommentLikeRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectCommentRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.notifications.services.NotificationService;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationType;
import com.ujenzilink.ujenzilink_backend.notifications.enums.NotificationPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.ujenzilink.ujenzilink_backend.user_mgt.enums.ActivityType;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.ActivityService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectCommentService {

    @Autowired
    private ProjectCommentRepository projectCommentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectCommentLikeRepository projectCommentLikeRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private NotificationService notificationService;

    public ApiCustomResponse<List<CommentDTO>> getProjectComments(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        User currentUser = securityUtil.getAuthenticatedUser().orElse(null);

        // Fetch all non-deleted comments for this project
        List<ProjectComment> allComments = projectCommentRepository
                .findByProjectAndIsDeletedFalseOrderByCreatedAtAsc(project);

        // Map to hold children for each parent
        Map<UUID, List<ProjectComment>> parentToChildren = new HashMap<>();
        List<ProjectComment> rootComments = new ArrayList<>();

        for (ProjectComment comment : allComments) {
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

        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "You must be logged in to comment", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        ProjectComment comment = new ProjectComment();
        comment.setProject(project);
        comment.setCommenter(currentUser);
        comment.setContent(request.text());

        if (request.parentId() != null) {
            ProjectComment parent = projectCommentRepository.findById(request.parentId()).orElse(null);
            if (parent == null || parent.isDeleted()) {
                return new ApiCustomResponse<>(null, "Parent comment not found", HttpStatus.NOT_FOUND.value());
            }
            comment.setParentComment(parent);
        }

        ProjectComment savedComment = projectCommentRepository.save(comment);

        // Log comment creation activity
        activityService.logActivity(currentUser, ActivityType.CREATE_COMMENT, savedComment.getId());

        // Send notifications
        if (savedComment.getParentComment() != null) {
            // It's a reply
            User parentCommenter = savedComment.getParentComment().getCommenter();
            if (parentCommenter != null && !parentCommenter.getId().equals(currentUser.getId())) {
                notificationService.createNotification(
                        parentCommenter,
                        currentUser,
                        NotificationType.PROJECT_COMMENT_REPLY,
                        "New Reply",
                        currentUser.getFirstName() + " replied to your comment on project " + project.getTitle() + ".",
                        NotificationPriority.LOW,
                        false,
                        null,
                        null);
            }
        } else {
            // It's a comment on the project
            User projectOwner = project.getOwner();
            if (projectOwner != null && !projectOwner.getId().equals(currentUser.getId())) {
                notificationService.createNotification(
                        projectOwner,
                        currentUser,
                        NotificationType.PROJECT_COMMENT,
                        "New Comment",
                        currentUser.getFirstName() + " commented on your project " + project.getTitle() + ".",
                        NotificationPriority.LOW,
                        false,
                        null,
                        null);
            }
        }

        // Map to DTO for response (newly created comment has no replies)
        CommentDTO responseDTO = mapToCommentDTO(savedComment, new ArrayList<>(), currentUser);

        return new ApiCustomResponse<>(responseDTO, "Comment created successfully", HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<String> likeComment(UUID commentId) {
        ProjectComment comment = projectCommentRepository.findById(commentId).orElse(null);
        if (comment == null || comment.isDeleted()) {
            return new ApiCustomResponse<>(null, "Comment not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "You must be logged in to like a comment",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        Optional<ProjectCommentLike> existingLike = projectCommentLikeRepository.findByCommentAndUser(comment,
                currentUser);

        if (existingLike.isPresent()) {
            projectCommentLikeRepository.delete(existingLike.get());
            activityService.logActivity(currentUser, ActivityType.UNLIKE_COMMENT, commentId);
            return new ApiCustomResponse<>(null, "Comment unliked successfully", HttpStatus.OK.value());
        } else {
            ProjectCommentLike commentLike = new ProjectCommentLike(comment, currentUser);
            projectCommentLikeRepository.save(commentLike);

            // Send notification to commenter
            User commenter = comment.getCommenter();
            if (commenter != null && !commenter.getId().equals(currentUser.getId())) {
                notificationService.createNotification(
                        commenter,
                        currentUser,
                        NotificationType.PROJECT_COMMENT_LIKE,
                        "Comment Liked",
                        currentUser.getFirstName() + " liked your comment.",
                        NotificationPriority.LOW,
                        false,
                        null,
                        null);
            }

            activityService.logActivity(currentUser, ActivityType.LIKE_COMMENT, commentId);
            return new ApiCustomResponse<>(null, "Comment liked successfully", HttpStatus.CREATED.value());
        }
    }

    private void collectDescendants(UUID parentId, Map<UUID, List<ProjectComment>> parentToChildren,
            List<ReplyDTO> flattenedReplies, User currentUser) {
        List<ProjectComment> children = parentToChildren.get(parentId);
        if (children != null) {
            for (ProjectComment child : children) {
                flattenedReplies.add(mapToReplyDTO(child, currentUser));
                // Recurse to find all descendants
                collectDescendants(child.getId(), parentToChildren, flattenedReplies, currentUser);
            }
        }
    }

    private CommentDTO mapToCommentDTO(ProjectComment comment, List<ReplyDTO> replies, User currentUser) {
        CreatorInfoDTO commenterInfo = mapToCreatorInfoDTO(comment.getCommenter());
        boolean hasLiked = currentUser != null
                && projectCommentLikeRepository.existsByCommentAndUser(comment, currentUser);
        int likesCount = (int) projectCommentLikeRepository.countByComment(comment);

        return new CommentDTO(
                comment.getId(),
                commenterInfo,
                comment.getContent(),
                comment.getCreatedAt(),
                hasLiked,
                likesCount,
                replies);
    }

    private ReplyDTO mapToReplyDTO(ProjectComment comment, User currentUser) {
        CreatorInfoDTO commenterInfo = mapToCreatorInfoDTO(comment.getCommenter());
        boolean hasLiked = currentUser != null
                && projectCommentLikeRepository.existsByCommentAndUser(comment, currentUser);
        int likesCount = (int) projectCommentLikeRepository.countByComment(comment);

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
