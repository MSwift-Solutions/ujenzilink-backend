package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectFollowDTO;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectFollow;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectFollowRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectLikeRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserMgtService {

    @Autowired
    private ProjectFollowRepository projectFollowRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectLikeRepository projectLikeRepository;

    public ApiCustomResponse<String> followProject(UUID projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userRepository.findFirstByEmail(authentication.getName());
        if (user == null) {
            return new ApiCustomResponse<>(null, "User not found", HttpStatus.NOT_FOUND.value());
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<ProjectFollow> existingFollow = projectFollowRepository.findByProjectAndUser(project, user);

        if (existingFollow.isPresent()) {
            ProjectFollow follow = existingFollow.get();
            if (follow.isActive()) {
                follow.setActive(false);
                follow.setEndDate(Instant.now());
                projectFollowRepository.save(follow);
                return new ApiCustomResponse<>("Unfollowed", "Project unfollowed successfully", HttpStatus.OK.value());
            } else {
                follow.setActive(true);
                follow.setEndDate(null);
                projectFollowRepository.save(follow);
                return new ApiCustomResponse<>("Followed", "Project followed successfully", HttpStatus.OK.value());
            }
        }

        ProjectFollow newFollow = new ProjectFollow(project, user);
        projectFollowRepository.save(newFollow);

        return new ApiCustomResponse<>("Followed", "Project followed successfully", HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<List<ProjectFollowDTO>> getProjectFollows(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        List<ProjectFollow> follows = projectFollowRepository.findByProjectAndIsActiveTrue(project);

        List<ProjectFollowDTO> dtos = follows.stream().map(follow -> {
            User follower = follow.getUser();
            String followerName = follower.getFullName();
            String profilePictureUrl = (follower.getProfilePicture() != null)
                    ? follower.getProfilePicture().getUrl()
                    : "https://ui-avatars.com/api/?name=" + followerName.replace(" ", "+")
                            + "&background=random";
            String username = (follower.getUserHandle() != null && !follower.getUserHandle().isEmpty())
                    ? follower.getUserHandle()
                    : follower.getEmail();

            CreatorInfoDTO followerInfo = new CreatorInfoDTO(followerName, username, profilePictureUrl);

            return new ProjectFollowDTO(
                    follow.getId(),
                    follow.getProject().getId(),
                    followerInfo,
                    follow.getCreatedAt(),
                    follow.getEndDate(),
                    follow.isNotificationsEnabled(),
                    follow.isActive());
        }).toList();

        return new ApiCustomResponse<>(dtos, "Project follows retrieved successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<Boolean> checkFollowStatus(UUID projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ApiCustomResponse<>(false, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userRepository.findFirstByEmail(authentication.getName());
        if (user == null) {
            return new ApiCustomResponse<>(false, "User not found", HttpStatus.NOT_FOUND.value());
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(false, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<ProjectFollow> follow = projectFollowRepository.findByProjectAndUser(project, user);
        boolean isFollowing = follow.isPresent() && follow.get().isActive();

        return new ApiCustomResponse<>(isFollowing, "Follow status checked successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<String> likeProject(UUID projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userRepository.findFirstByEmail(authentication.getName());
        if (user == null) {
            return new ApiCustomResponse<>(null, "User not found", HttpStatus.NOT_FOUND.value());
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<com.ujenzilink.ujenzilink_backend.projects.models.ProjectLike> existingLike = projectLikeRepository
                .findByProjectAndUser(project, user);

        if (existingLike.isPresent()) {
            projectLikeRepository.delete(existingLike.get());
            return new ApiCustomResponse<>("Unliked", "Project unliked successfully", HttpStatus.OK.value());
        }

        com.ujenzilink.ujenzilink_backend.projects.models.ProjectLike newLike = new com.ujenzilink.ujenzilink_backend.projects.models.ProjectLike(
                project, user);
        projectLikeRepository.save(newLike);

        return new ApiCustomResponse<>("Liked", "Project liked successfully", HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<Boolean> checkLikeStatus(UUID projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ApiCustomResponse<>(false, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userRepository.findFirstByEmail(authentication.getName());
        if (user == null) {
            return new ApiCustomResponse<>(false, "User not found", HttpStatus.NOT_FOUND.value());
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(false, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        boolean isLiked = projectLikeRepository.existsByProjectAndUser(project, user);

        return new ApiCustomResponse<>(isLiked, "Like status checked successfully", HttpStatus.OK.value());
    }
}
