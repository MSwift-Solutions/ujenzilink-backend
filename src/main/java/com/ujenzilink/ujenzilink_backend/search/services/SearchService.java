package com.ujenzilink.ujenzilink_backend.search.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostImage;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostImageRepository;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.StagePhoto;
import com.ujenzilink.ujenzilink_backend.projects.repositories.StagePhotoRepository;
import com.ujenzilink.ujenzilink_backend.search.dtos.*;
import com.ujenzilink.ujenzilink_backend.search.repositories.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates unified full-text search across Users, Projects, and Posts.
 *
 * <p>Search approach:
 * <ol>
 *   <li>Validates and sanitises the query string.</li>
 *   <li>Runs three parallel pairs of (search + count) native SQL queries using
 *       {@link SearchRepository}.</li>
 *   <li>Maps raw JPA entities to lean search result DTOs.</li>
 *   <li>Returns up to {@code limit} results per category and the full match count
 *       per category so the frontend can show "400 results found".</li>
 * </ol>
 *
 * <p>Requires the {@code pg_trgm} extension on the PostgreSQL instance:
 * {@code CREATE EXTENSION IF NOT EXISTS pg_trgm;}
 */
@Service
public class SearchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private StagePhotoRepository stagePhotoRepository;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * Perform a unified search across people, projects, and posts.
     *
     * @param query raw query string from the client
     * @param limit max results to return per category (1–50); defaults to 10
     * @return {@link ApiCustomResponse} wrapping a {@link SearchResponse}
     */
    @Transactional(readOnly = true)
    public ApiCustomResponse<SearchResponse> search(String query, Integer limit) {

        // ── Auth check ──────────────────────────────────────────────────────
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        // ── Validate query ──────────────────────────────────────────────────
        if (query == null || query.isBlank()) {
            return new ApiCustomResponse<>(null, "Search query must not be blank",
                    HttpStatus.BAD_REQUEST.value());
        }

        String trimmed = query.trim();
        if (trimmed.length() < 2) {
            return new ApiCustomResponse<>(null, "Search query must be at least 2 characters",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (trimmed.length() > 200) {
            return new ApiCustomResponse<>(null, "Search query must not exceed 200 characters",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Sanitise: remove characters that break plainto_tsquery
        String sanitised = trimmed.replaceAll("[!&|<>():*]", " ").trim();

        // ── Normalise limit ─────────────────────────────────────────────────
        int effectiveLimit = (limit == null || limit < 1) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        // ── Run searches ────────────────────────────────────────────────────
        List<User>    users    = searchRepository.searchUsers(sanitised, sanitised, effectiveLimit);
        long          totalPeople   = searchRepository.countSearchUsers(sanitised, sanitised);

        List<Project> projects = searchRepository.searchProjects(sanitised, sanitised, effectiveLimit);
        long          totalProjects = searchRepository.countSearchProjects(sanitised, sanitised);

        List<Post>    posts    = searchRepository.searchPosts(sanitised, sanitised, effectiveLimit);
        long          totalPosts    = searchRepository.countSearchPosts(sanitised, sanitised);

        // ── Map results ─────────────────────────────────────────────────────
        List<SearchPeopleResult>  peopleResults  = users.stream()
                .map(this::mapUserToSearchResult).toList();

        List<SearchProjectResult> projectResults = projects.stream()
                .map(this::mapProjectToSearchResult).toList();

        List<SearchPostResult>    postResults    = posts.stream()
                .map(this::mapPostToSearchResult).toList();

        SearchResultCounts counts = new SearchResultCounts(totalPeople, totalProjects, totalPosts);
        SearchResponse response   = new SearchResponse(peopleResults, projectResults, postResults, counts);

        return new ApiCustomResponse<>(response,
                "Search completed successfully",
                HttpStatus.OK.value());
    }

    // ─── Mapper helpers ──────────────────────────────────────────────────────

    private SearchPeopleResult mapUserToSearchResult(User user) {
        String name = user.getFullName();
        String username = resolveUsername(user);
        String profilePictureUrl = resolveProfilePicture(user, name);
        String lastActivity = user.getLastSuccessfulLogin() != null
                ? formatLastActivity(user.getLastSuccessfulLogin())
                : null;

        return new SearchPeopleResult(
                user.getId(),
                name,
                username,
                profilePictureUrl,
                user.getBio(),
                user.getLocation(),
                user.getSkills(),
                lastActivity);
    }

    private SearchProjectResult mapProjectToSearchResult(Project project) {
        User owner = project.getOwner();
        String ownerName = owner.getFullName();
        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(
                owner.getId(),
                ownerName,
                resolveUsername(owner),
                resolveProfilePicture(owner, ownerName));

        // Use the most recent stage photo as a thumbnail if available, otherwise null
        List<StagePhoto> stagePhotos = stagePhotoRepository
                .findByStage_Project_IdOrderByUploadedAtDesc(project.getId());
        String thumbnail = stagePhotos.isEmpty() ? null
                : (stagePhotos.get(0).getImage() != null ? stagePhotos.get(0).getImage().getUrl() : null);

        return new SearchProjectResult(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getProjectType(),
                project.getProjectStatus(),
                project.getLocation(),
                creatorInfo,
                thumbnail,
                project.getCreatedAt(),
                project.getUpdatedAt());
    }

    private SearchPostResult mapPostToSearchResult(Post post) {
        User creator = post.getCreator();
        String creatorName = creator.getFullName();
        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(
                creator.getId(),
                creatorName,
                resolveUsername(creator),
                resolveProfilePicture(creator, creatorName));

        List<String> images = postImageRepository
                .findByPostOrderByImageOrderAsc(post)
                .stream()
                .map(PostImage::getImage)
                .filter(img -> img != null && !img.getIsDeleted())
                .map(img -> img.getUrl())
                .toList();

        return new SearchPostResult(
                post.getId(),
                post.getContent(),
                creatorInfo,
                images,
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    // ─── Utility helpers ─────────────────────────────────────────────────────

    private String resolveUsername(User user) {
        return (user.getUserHandle() != null && !user.getUserHandle().isBlank())
                ? user.getUserHandle()
                : user.getEmail();
    }

    private String resolveProfilePicture(User user, String fullName) {
        return (user.getProfilePicture() != null)
                ? user.getProfilePicture().getUrl()
                : "https://ui-avatars.com/api/?name=" + fullName.replace(" ", "+") + "&background=random";
    }

    /**
     * Human-readable relative time from a past {@link Instant}, e.g. "3 hours ago".
     * Mirrors the implementation in {@code UserMgtService} for consistency.
     */
    private String formatLastActivity(Instant lastLogin) {
        Duration duration = Duration.between(lastLogin, Instant.now());
        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours   = duration.toHours();
        long days    = duration.toDays();

        if (seconds < 60)  return "Just now";
        if (minutes < 60)  return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        if (hours < 24)    return hours   + (hours   == 1 ? " hour ago"   : " hours ago");
        if (days < 7)      return days    + (days    == 1 ? " day ago"    : " days ago");
        long weeks = days / 7;
        if (days < 30)     return weeks   + (weeks   == 1 ? " week ago"   : " weeks ago");
        long months = days / 30;
        if (days < 365)    return months  + (months  == 1 ? " month ago"  : " months ago");
        long years = days / 365;
        return years + (years == 1 ? " year ago" : " years ago");
    }
}
