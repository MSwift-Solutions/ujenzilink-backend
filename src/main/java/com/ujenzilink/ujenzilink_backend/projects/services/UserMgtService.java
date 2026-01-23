package com.ujenzilink.ujenzilink_backend.projects.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectFollowDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectLikeDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.TeamMemberSearchDTO;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectFollow;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectLike;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectMember;
import com.ujenzilink.ujenzilink_backend.projects.enums.MemberRole;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectFollowRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectLikeRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectMemberRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private SecurityUtil securityUtil;

    public ApiCustomResponse<String> followProject(UUID projectId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

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

            CreatorInfoDTO followerInfo = new CreatorInfoDTO(follower.getId(), followerName, username,
                    profilePictureUrl);

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
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(false, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(false, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<ProjectFollow> follow = projectFollowRepository.findByProjectAndUser(project, user);
        boolean isFollowing = follow.isPresent() && follow.get().isActive();

        return new ApiCustomResponse<>(isFollowing, "Follow status checked successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<String> likeProject(UUID projectId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        Optional<ProjectLike> existingLike = projectLikeRepository.findByProjectAndUser(project, user);

        if (existingLike.isPresent()) {
            projectLikeRepository.delete(existingLike.get());
            return new ApiCustomResponse<>("Unliked", "Project unliked successfully", HttpStatus.OK.value());
        }

        ProjectLike newLike = new ProjectLike(project, user);
        projectLikeRepository.save(newLike);

        return new ApiCustomResponse<>("Liked", "Project liked successfully", HttpStatus.CREATED.value());
    }

    public ApiCustomResponse<Boolean> checkLikeStatus(UUID projectId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(false, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(false, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        boolean isLiked = projectLikeRepository.existsByProjectAndUser(project, user);

        return new ApiCustomResponse<>(isLiked, "Like status checked successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<List<ProjectLikeDTO>> getProjectLikes(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        List<ProjectLike> likes = projectLikeRepository.findByProject(project);

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

        return new ApiCustomResponse<>(dtos, "Project likes retrieved successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<List<TeamMemberSearchDTO>> searchTeamMembers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ApiCustomResponse<>(List.of(), "Search term is required", HttpStatus.BAD_REQUEST.value());
        }

        // Search users with limit of 10
        List<User> users = userRepository.searchUsers(searchTerm.trim(), PageRequest.of(0, 10));

        Random random = new Random();
        List<TeamMemberSearchDTO> results = users.stream().map(user -> {
            // Get user name
            String name = user.getFullName();

            // Get profile picture URL
            String profilePictureUrl = (user.getProfilePicture() != null)
                    ? user.getProfilePicture().getUrl()
                    : "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random";

            // Get username with fallback priority: username -> email
            String username;
            if (user.getUserHandle() != null && !user.getUserHandle().isEmpty()) {
                username = user.getUserHandle();
            } else {
                username = user.getEmail();
            }

            // Randomize online/offline status (70% online, 30% offline)
            String status = random.nextInt(100) < 70 ? "online" : "offline";

            // Format last activity from lastSuccessfulLogin
            String lastActivity;
            Instant lastLogin = user.getLastSuccessfulLogin();

            if (lastLogin == null) {
                lastActivity = "Never logged in";
            } else {
                lastActivity = formatLastActivity(lastLogin);
            }

            return new TeamMemberSearchDTO(
                    user.getId(),
                    name,
                    username,
                    profilePictureUrl,
                    status,
                    lastActivity);
        }).toList();

        return new ApiCustomResponse<>(results, "Team members retrieved successfully", HttpStatus.OK.value());
    }

    public ApiCustomResponse<String> addMember(UUID projectId, UUID userId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isDeleted()) {
            return new ApiCustomResponse<>(null, "Project not found", HttpStatus.NOT_FOUND.value());
        }

        User memberToAdd = userRepository.findById(userId).orElse(null);
        if (memberToAdd == null) {
            return new ApiCustomResponse<>(null, "Member user not found", HttpStatus.NOT_FOUND.value());
        }

        if (projectMemberRepository.existsByProjectAndUser(project, memberToAdd)) {
            return new ApiCustomResponse<>(null, "User is already a member of this project",
                    HttpStatus.CONFLICT.value());
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(memberToAdd);
        member.setAddedBy(currentUser);
        member.setRole(MemberRole.OWNER);
        member.setCanViewProject(true);
        member.setCanManageStages(true);
        member.setCanCreatePosts(true);
        member.setCanUploadDocuments(true);
        member.setCanManageMembers(true);

        projectMemberRepository.save(member);

        return new ApiCustomResponse<>("Member Added", "Member added to project successfully",
                HttpStatus.CREATED.value());
    }

    private String formatLastActivity(Instant lastLogin) {
        Instant now = Instant.now();
        Duration duration = Duration.between(lastLogin, now);

        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (days < 365) {
            long months = days / 30;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = days / 365;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }
}
