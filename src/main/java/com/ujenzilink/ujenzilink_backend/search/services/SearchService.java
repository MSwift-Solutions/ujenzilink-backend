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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
        List<User> users      = searchRepository.searchUsers(sanitised, sanitised, effectiveLimit);
        long       totalPeople = searchRepository.countSearchUsers(sanitised, sanitised);

        List<Project> projects      = searchRepository.searchProjects(sanitised, sanitised, effectiveLimit);
        long          totalProjects = searchRepository.countSearchProjects(sanitised, sanitised);

        List<Post> posts      = searchRepository.searchPosts(sanitised, sanitised, effectiveLimit);
        long       totalPosts = searchRepository.countSearchPosts(sanitised, sanitised);

        // ── Fallback: if primary text search returned NOTHING, search by person name ──
        if (projects.isEmpty()) {
            projects = searchRepository.searchProjectsByPersonName(sanitised, sanitised, effectiveLimit);
            if (!projects.isEmpty()) {
                totalProjects = searchRepository.countSearchProjectsByPersonName(sanitised, sanitised);
            }
        }

        if (posts.isEmpty()) {
            posts = searchRepository.searchPostsByPersonName(sanitised, sanitised, effectiveLimit);
            if (!posts.isEmpty()) {
                totalPosts = searchRepository.countSearchPostsByPersonName(sanitised, sanitised);
            }
        }

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

    @Transactional(readOnly = true)
    public ApiCustomResponse<SearchResponse> getSampleData() {
        // ── Auth check ──────────────────────────────────────────────────────
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        int sampleLimit = 5;

        // ── Fetch random data ───────────────────────────────────────────────
        List<User> users       = searchRepository.findRandomUsers(sampleLimit);
        List<Project> projects = searchRepository.findRandomProjects(sampleLimit);
        List<Post> posts       = searchRepository.findRandomPosts(sampleLimit);

        // ── Map results ─────────────────────────────────────────────────────
        List<SearchPeopleResult>  peopleResults  = users.stream()
                .map(this::mapUserToSearchResult).toList();

        List<SearchProjectResult> projectResults = projects.stream()
                .map(this::mapProjectToSearchResult).toList();

        List<SearchPostResult>    postResults    = posts.stream()
                .map(this::mapPostToSearchResult).toList();

        // ── Counts are set to 0 as per requirement ──────────────────────────
        SearchResultCounts counts = new SearchResultCounts(0, 0, 0);
        SearchResponse response   = new SearchResponse(peopleResults, projectResults, postResults, counts);

        return new ApiCustomResponse<>(response,
                "Sample data fetched successfully",
                HttpStatus.OK.value());
    }

    // ─── Mapper helpers ──────────────────────────────────────────────────────

    private SearchPeopleResult mapUserToSearchResult(User user) {
        String name = user.getFullName();
        String username = resolveUsername(user);
        String profilePictureUrl = resolveProfilePicture(user, name);

        return new SearchPeopleResult(
                user.getId(),
                name,
                username,
                profilePictureUrl,
                user.getBio(),
                user.getLocation(),
                user.getSkills(),
                user.getLastSuccessfulLogin());
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
}
