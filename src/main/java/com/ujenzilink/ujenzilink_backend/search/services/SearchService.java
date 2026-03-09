package com.ujenzilink.ujenzilink_backend.search.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostImage;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostImageRepository;
import com.ujenzilink.ujenzilink_backend.posts.dtos.PostListResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectListResponse;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import com.ujenzilink.ujenzilink_backend.projects.models.StagePhoto;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectCommentRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectFollowRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectLikeRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectMemberRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectStageRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.StagePhotoRepository;
import com.ujenzilink.ujenzilink_backend.projects.utils.ProjectUtils;
import com.ujenzilink.ujenzilink_backend.search.dtos.*;
import com.ujenzilink.ujenzilink_backend.search.repositories.SearchRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.posts.repositories.PostRepository;
import com.ujenzilink.ujenzilink_backend.user_mgt.models.Bio;
import com.ujenzilink.ujenzilink_backend.user_mgt.repositories.BioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Base64;
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
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectStageRepository projectStageRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectFollowRepository projectFollowRepository;

    @Autowired
    private ProjectLikeRepository projectLikeRepository;

    @Autowired
    private ProjectCommentRepository projectCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BioRepository bioRepository;

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
    public ApiCustomResponse<PeoplePageResponse> searchPeoplePaginated(String query, String cursor, Integer limit) {

        // ── Auth check ──────────────────────────────────────────────────────
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        // ── Validate query ──────────────────────────────────────────────────
        // For the full people list, we allow empty query.
        String sanitised = "";
        boolean isQueryEmpty = true;
        
        if (query != null && !query.isBlank()) {
            String trimmed = query.trim();
            if (trimmed.length() > 200) {
                return new ApiCustomResponse<>(null, "Search query must not exceed 200 characters", HttpStatus.BAD_REQUEST.value());
            }
            sanitised = trimmed.replaceAll("[!&|<>():*]", " ").trim();
            isQueryEmpty = sanitised.isEmpty();
        }

        int effectiveLimit = (limit == null || limit < 1) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        Instant cursorTime = Instant.now();
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decodedJson = new String(Base64.getDecoder().decode(cursor));
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> cursorData = mapper.readValue(decodedJson, Map.class);
                if (cursorData.containsKey("timestamp")) {
                    cursorTime = Instant.parse((String) cursorData.get("timestamp"));
                }
            } catch (Exception e) {
                return new ApiCustomResponse<>(null, "Invalid cursor format", HttpStatus.BAD_REQUEST.value());
            }
        }

        // Fetch limit + 1 to determine if there are more
        List<User> users;
        if (isQueryEmpty) {
            users = searchRepository.findAllUsersPaginated(effectiveLimit + 1, cursorTime);
        } else {
            users = searchRepository.searchUsersPaginated(sanitised, sanitised, effectiveLimit + 1, cursorTime);
        }
        
        boolean hasMore = users.size() > effectiveLimit;
        if (hasMore) {
            users = users.subList(0, effectiveLimit);
        }

        List<PersonDTO> people = users.stream().map(u -> {
            int projCount = (int) projectRepository.countByOwner_IdAndIsDeletedFalse(u.getId());
            int postCount = (int) postRepository.countByCreator_IdAndIsDeletedFalse(u.getId());
            
            // Get professional info from Bio
            Bio bio = bioRepository.findByUser(u).orElse(new Bio());
            String roleStr = (bio.getTitle() != null && !bio.getTitle().isBlank()) 
                    ? bio.getTitle() 
                    : "";
            int yearsInConstruction = (bio.getYearsOfExperience() != null) ? bio.getYearsOfExperience() : 0;

            String avatarUrl = resolveProfilePicture(u, u.getFullName());
            String loc = (u.getLocation() != null) ? u.getLocation() : "";
            
            return new PersonDTO(
                    u.getId(),
                    u.getFullName(),
                    resolveUsername(u),
                    avatarUrl,
                    roleStr,
                    loc,
                    projCount,
                    postCount,
                    bio.getBio(),
                    yearsInConstruction
            );
        }).toList();

        String nextCursor = null;
        if (hasMore && !users.isEmpty()) {
            try {
                User lastUser = users.get(users.size() - 1);
                String cursorJson = String.format("{\"timestamp\":\"%s\"}", lastUser.getDateOfCreation().toString());
                nextCursor = Base64.getEncoder().encodeToString(cursorJson.getBytes());
            } catch (Exception e) {
                // Ignore cursor generation errors
            }
        }

        long totalPeople;
        if (isQueryEmpty) {
            totalPeople = searchRepository.countAllActiveUsers();
        } else {
            totalPeople = searchRepository.countSearchUsers(sanitised, sanitised);
        }

        PeoplePageResponse pageResponse = new PeoplePageResponse(people, nextCursor, hasMore, totalPeople);

        return new ApiCustomResponse<>(pageResponse, "Paginated people retrieved successfully", HttpStatus.OK.value());
    }

    @Transactional(readOnly = true)
    public ApiCustomResponse<ProjectSearchPageResponse> searchProjectsPaginated(String query, String cursor, Integer limit) {

        // ── Auth check ──────────────────────────────────────────────────────
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized", HttpStatus.UNAUTHORIZED.value());
        }

        // ── Require non-empty query ──────────────────────────────────────────
        if (query == null || query.isBlank()) {
            return new ApiCustomResponse<>(null, "Search query must not be blank", HttpStatus.BAD_REQUEST.value());
        }

        String trimmed = query.trim();
        if (trimmed.length() < 2) {
            return new ApiCustomResponse<>(null, "Search query must be at least 2 characters", HttpStatus.BAD_REQUEST.value());
        }
        if (trimmed.length() > 200) {
            return new ApiCustomResponse<>(null, "Search query must not exceed 200 characters", HttpStatus.BAD_REQUEST.value());
        }

        String sanitised = trimmed.replaceAll("[!&|<>():*]", " ").trim();
        if (sanitised.isEmpty()) {
            return new ApiCustomResponse<>(null, "Search query contains no valid characters", HttpStatus.BAD_REQUEST.value());
        }

        int effectiveLimit = (limit == null || limit < 1) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        // ── Decode cursor ────────────────────────────────────────────────────
        Instant cursorTime = Instant.now();
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decodedJson = new String(Base64.getDecoder().decode(cursor));
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> cursorData = mapper.readValue(decodedJson, Map.class);
                if (cursorData.containsKey("timestamp")) {
                    cursorTime = Instant.parse((String) cursorData.get("timestamp"));
                }
            } catch (Exception e) {
                return new ApiCustomResponse<>(null, "Invalid cursor format", HttpStatus.BAD_REQUEST.value());
            }
        }

        // ── Fetch limit + 1 to determine hasMore ─────────────────────────────
        List<Project> projects = searchRepository.searchProjectsPaginated(sanitised, sanitised, effectiveLimit + 1, cursorTime);

        boolean hasMore = projects.size() > effectiveLimit;
        if (hasMore) {
            projects = projects.subList(0, effectiveLimit);
        }

        // ── Fallback: if primary search returned nothing, search by person name ──
        long totalProjects;
        if (projects.isEmpty()) {
            projects = searchRepository.searchProjectsByPersonNamePaginated(sanitised, sanitised, effectiveLimit + 1, cursorTime);
            hasMore = projects.size() > effectiveLimit;
            if (hasMore) {
                projects = projects.subList(0, effectiveLimit);
            }
            totalProjects = projects.isEmpty() ? 0 : searchRepository.countSearchProjectsByPersonName(sanitised, sanitised);
        } else {
            totalProjects = searchRepository.countSearchProjectsPaginated(sanitised, sanitised);
        }

        // ── Map to ProjectListResponse ────────────────────────────────────────
        List<ProjectListResponse> projectResponses = projects.stream()
                .map(this::mapProjectToProjectListResponse)
                .collect(java.util.stream.Collectors.toList());

        // ── Generate next cursor ─────────────────────────────────────────────
        String nextCursor = null;
        if (hasMore && !projects.isEmpty()) {
            try {
                Project lastProject = projects.get(projects.size() - 1);
                String cursorJson = String.format("{\"timestamp\":\"%s\"}", lastProject.getCreatedAt().toString());
                nextCursor = Base64.getEncoder().encodeToString(cursorJson.getBytes());
            } catch (Exception e) {
                // Ignore cursor generation errors
            }
        }

        ProjectSearchPageResponse pageResponse = new ProjectSearchPageResponse(projectResponses, nextCursor, hasMore, totalProjects);

        return new ApiCustomResponse<>(pageResponse, "Project search results retrieved successfully", HttpStatus.OK.value());
    }

    // ─── POSTS SEARCH (PAGINATED) ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApiCustomResponse<PostSearchPageResponse> searchPostsPaginated(
            String query,
            String cursor,
            Integer limit) {

        // ── Auth validation ──────────────────────────────────────────────────
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Unauthorized access", HttpStatus.UNAUTHORIZED.value());
        }

        // ── Input & limit validation ─────────────────────────────────────────
        if (query == null || query.trim().isEmpty()) {
            return new ApiCustomResponse<>(null, "Search query cannot be empty", HttpStatus.BAD_REQUEST.value());
        }

        String sanitised = query.trim().replaceAll("\\s+", " ");
        if (sanitised.length() < 2) {
            return new ApiCustomResponse<>(null, "Search query must be at least 2 characters", HttpStatus.BAD_REQUEST.value());
        }
        if (sanitised.length() > 100) {
            return new ApiCustomResponse<>(null, "Search query too long (max 100 chars)", HttpStatus.BAD_REQUEST.value());
        }

        int effectiveLimit = (limit == null || limit < 1) ? 20 : limit;
        if (effectiveLimit > 100) effectiveLimit = 100;

        // ── Decode cursor ────────────────────────────────────────────────────
        Instant cursorTime = Instant.now();
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                String decodedJson = new String(Base64.getDecoder().decode(cursor));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> cursorData = mapper.readValue(decodedJson, Map.class);

                if (cursorData.containsKey("timestamp")) {
                    cursorTime = Instant.parse((String) cursorData.get("timestamp"));
                }
            } catch (Exception e) {
                return new ApiCustomResponse<>(null, "Invalid cursor format", HttpStatus.BAD_REQUEST.value());
            }
        }

        // ── Fetch limit + 1 to determine hasMore ─────────────────────────────
        List<Post> posts = searchRepository.searchPostsPaginated(sanitised, sanitised, effectiveLimit + 1, cursorTime);

        boolean hasMore = posts.size() > effectiveLimit;
        if (hasMore) {
            posts = posts.subList(0, effectiveLimit);
        }

        // ── Fallback: if primary search returned 0, search by person name ────
        long totalPosts;
        if (posts.isEmpty()) {
            posts = searchRepository.searchPostsByPersonNamePaginated(sanitised, sanitised, effectiveLimit + 1, cursorTime);
            hasMore = posts.size() > effectiveLimit;
            if (hasMore) {
                posts = posts.subList(0, effectiveLimit);
            }
            totalPosts = posts.isEmpty() ? 0 : searchRepository.countSearchPostsByPersonName(sanitised, sanitised);
        } else {
            totalPosts = searchRepository.countSearchPostsPaginated(sanitised, sanitised);
        }

        // ── Map results to PostListResponse ──────────────────────────────────
        List<PostListResponse> postResponses = posts.stream()
                .map(this::mapPostToPostListResponse)
                .collect(java.util.stream.Collectors.toList());

        // ── Generate next cursor ─────────────────────────────────────────────
        String nextCursor = null;
        if (hasMore && !posts.isEmpty()) {
            try {
                Post lastPost = posts.get(posts.size() - 1);
                String cursorJson = String.format("{\"timestamp\":\"%s\"}", lastPost.getCreatedAt().toString());
                nextCursor = Base64.getEncoder().encodeToString(cursorJson.getBytes());
            } catch (Exception e) {
                // Ignore cursor generation errors
            }
        }

        PostSearchPageResponse pageResponse = new PostSearchPageResponse(postResponses, nextCursor, hasMore, totalPosts);

        return new ApiCustomResponse<>(pageResponse, "Post search results retrieved successfully", HttpStatus.OK.value());
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

    /**
     * Reusable helper: maps a Project entity to a ProjectListResponse.
     * Mirrors the mapping logic in ProjectService.getAllProjects.
     */
    private ProjectListResponse mapProjectToProjectListResponse(Project project) {
        User creator = project.getCreatedBy();
        String creatorName = creator.getFullName();
        CreatorInfoDTO creatorInfo = new CreatorInfoDTO(
                creator.getId(), creatorName, resolveUsername(creator), resolveProfilePicture(creator, creatorName));

        int memberCount = projectMemberRepository.findByProjectAndIsDeletedFalse(project).size();

        List<ProjectStage> stages = projectStageRepository.findByProjectOrderByCreatedAtAsc(project);
        List<String> projectImages = new ArrayList<>();
        List<ProjectStage> reversedStages = new ArrayList<>(stages);
        Collections.reverse(reversedStages);
        for (ProjectStage stage : reversedStages) {
            if (projectImages.size() >= 3) break;
            List<StagePhoto> stagePhotos = stagePhotoRepository.findByStageOrderByPhotoOrder(stage);
            for (StagePhoto photo : stagePhotos) {
                if (photo.getImage() != null && !photo.getImage().getIsDeleted()) {
                    projectImages.add(photo.getImage().getUrl());
                    if (projectImages.size() >= 3) break;
                }
            }
        }

        String currentStage;
        if (!stages.isEmpty()) {
            ProjectStage activeStage = stages.stream()
                    .filter(s -> s.getStatus().name().contains("IN_PROGRESS")
                            || s.getStatus().name().equals("PLANNING_PERMITS"))
                    .findFirst()
                    .orElse(stages.get(stages.size() - 1));
            currentStage = ProjectUtils.formatEnumName(activeStage.getStatus().name());
        } else {
            currentStage = ProjectUtils.formatEnumName("PLANNING_PERMITS");
        }

        int followCount   = (int) projectFollowRepository.countByProjectAndIsActiveTrue(project);
        int likesCount    = (int) projectLikeRepository.countByProject(project);
        int commentsCount = (int) projectCommentRepository.countByProjectAndIsDeletedFalse(project);

        return new ProjectListResponse(
                project.getId(),
                project.getTitle(),
                project.getProjectType(),
                project.getProjectStatus(),
                project.getLocation(),
                project.getCreatedAt(),
                creatorInfo,
                memberCount,
                projectImages,
                project.getEstimatedBudget(),
                project.getCurrency(),
                likesCount,
                commentsCount,
                followCount,
                currentStage);
    }


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

    /**
     * Reusable helper: maps a Post entity to a PostListResponse.
     * Mirrors the mapping logic in PostService.mapToPostListResponse.
     */
    private PostListResponse mapPostToPostListResponse(Post post) {
        User creator = post.getCreator();
        String creatorName = creator.getFullName();
        String username = resolveUsername(creator);
        String profilePictureUrl = resolveProfilePicture(creator, creatorName);
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
