package com.ujenzilink.ujenzilink_backend.search.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository providing PostgreSQL Full-Text Search (FTS) queries for the unified search endpoint.
 *
 * <p>Strategy:
 * <ul>
 *   <li>Primary ranking: {@code ts_rank} on a computed {@code tsvector}</li>
 *   <li>Fallback for short / typo queries: {@code pg_trgm} similarity via ILIKE trigram patterns</li>
 *   <li>Each search has a paired COUNT query so the service can return total match sizes
 *       without fetching all rows.</li>
 * </ul>
 *
 * <p>Requirements: {@code pg_trgm} extension must be enabled in the database.
 * Run {@code CREATE EXTENSION IF NOT EXISTS pg_trgm;} once on the target PostgreSQL instance.
 */
@Repository
public interface SearchRepository extends JpaRepository<User, UUID> {

    // ─── USERS ────────────────────────────────────────────────────────────────

    /**
     * Full-text + trigram search across first name, last name, username, bio, skills, and location.
     *
     * <p>The query combines two strategies with OR so both exact FTS matches and fuzzy ILIKE
     * matches are included, then ranks by FTS relevance descending.
     */
    @Query(value = """
            SELECT u.*
            FROM users u
            WHERE u.is_deleted = false
              AND u.is_enabled = true
              AND (
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '') || ' ' ||
                        COALESCE(u.username, '') || ' ' ||
                        COALESCE(u.bio, '') || ' ' ||
                        COALESCE(u.skills, '') || ' ' ||
                        COALESCE(u.location, '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(CONCAT(u.first_name, ' ', COALESCE(u.middle_name, ''), ' ', u.last_name))
                        ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(u.username) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY
                ts_rank(
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '') || ' ' ||
                        COALESCE(u.username, '') || ' ' ||
                        COALESCE(u.bio, '') || ' ' ||
                        COALESCE(u.skills, '') || ' ' ||
                        COALESCE(u.location, '')
                    ),
                    plainto_tsquery('english', :query)
                ) DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<User> searchUsers(@Param("query") String query,
                           @Param("rawQuery") String rawQuery,
                           @Param("limit") int limit);

    /**
     * Count of users matching the search — used to populate {@code SearchResultCounts.totalPeople}.
     */
    @Query(value = """
            SELECT COUNT(u.id)
            FROM users u
            WHERE u.is_deleted = false
              AND u.is_enabled = true
              AND (
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '') || ' ' ||
                        COALESCE(u.username, '') || ' ' ||
                        COALESCE(u.bio, '') || ' ' ||
                        COALESCE(u.skills, '') || ' ' ||
                        COALESCE(u.location, '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(CONCAT(u.first_name, ' ', COALESCE(u.middle_name, ''), ' ', u.last_name))
                        ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(u.username) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            """,
            nativeQuery = true)
    long countSearchUsers(@Param("query") String query,
                          @Param("rawQuery") String rawQuery);

    // ─── PROJECTS ─────────────────────────────────────────────────────────────

    /**
     * Full-text + trigram search over project title, description, and location.
     * Only PUBLIC, non-deleted projects are included.
     */
    @Query(value = """
            SELECT p.*
            FROM projects p
            WHERE p.is_deleted = false
              AND p.visibility = 'PUBLIC'
              AND (
                    to_tsvector('english',
                        COALESCE(p.title, '') || ' ' ||
                        COALESCE(p.description, '') || ' ' ||
                        COALESCE(p.location, '') || ' ' ||
                        COALESCE(CAST(p.project_type AS TEXT), '') || ' ' ||
                        COALESCE(CAST(p.project_status AS TEXT), '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(p.title) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(p.location) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY
                ts_rank(
                    to_tsvector('english',
                        COALESCE(p.title, '') || ' ' ||
                        COALESCE(p.description, '') || ' ' ||
                        COALESCE(p.location, '') || ' ' ||
                        COALESCE(CAST(p.project_type AS TEXT), '') || ' ' ||
                        COALESCE(CAST(p.project_status AS TEXT), '')
                    ),
                    plainto_tsquery('english', :query)
                ) DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Project> searchProjects(@Param("query") String query,
                                 @Param("rawQuery") String rawQuery,
                                 @Param("limit") int limit);

    /**
     * Count of projects matching the search — used to populate {@code SearchResultCounts.totalProjects}.
     */
    @Query(value = """
            SELECT COUNT(p.id)
            FROM projects p
            WHERE p.is_deleted = false
              AND p.visibility = 'PUBLIC'
              AND (
                    to_tsvector('english',
                        COALESCE(p.title, '') || ' ' ||
                        COALESCE(p.description, '') || ' ' ||
                        COALESCE(p.location, '') || ' ' ||
                        COALESCE(CAST(p.project_type AS TEXT), '') || ' ' ||
                        COALESCE(CAST(p.project_status AS TEXT), '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(p.title) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(p.location) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            """,
            nativeQuery = true)
    long countSearchProjects(@Param("query") String query,
                              @Param("rawQuery") String rawQuery);

    // ─── POSTS ────────────────────────────────────────────────────────────────

    /**
     * Full-text + trigram search over post content.
     * Only non-deleted posts are included.
     */
    @Query(value = """
            SELECT po.*
            FROM posts po
            WHERE po.is_deleted = false
              AND (
                    to_tsvector('english', COALESCE(po.content, ''))
                        @@ plainto_tsquery('english', :query)
                    OR LOWER(po.content) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY
                ts_rank(
                    to_tsvector('english', COALESCE(po.content, '')),
                    plainto_tsquery('english', :query)
                ) DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Post> searchPosts(@Param("query") String query,
                           @Param("rawQuery") String rawQuery,
                           @Param("limit") int limit);

    /**
     * Count of posts matching the search — used to populate {@code SearchResultCounts.totalPosts}.
     */
    @Query(value = """
            SELECT COUNT(po.id)
            FROM posts po
            WHERE po.is_deleted = false
              AND (
                    to_tsvector('english', COALESCE(po.content, ''))
                        @@ plainto_tsquery('english', :query)
                    OR LOWER(po.content) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            """,
            nativeQuery = true)
    long countSearchPosts(@Param("query") String query,
                          @Param("rawQuery") String rawQuery);
}
