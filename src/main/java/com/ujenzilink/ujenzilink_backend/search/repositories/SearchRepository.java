package com.ujenzilink.ujenzilink_backend.search.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SearchRepository extends JpaRepository<User, UUID> {

    // ─── USERS ───────────────────────────────────────────────────────────────

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

    @Query(value = """
            SELECT u.*
            FROM users u
            WHERE u.is_deleted = false
              AND u.is_enabled = true
              AND u.date_of_creation < :cursor
            ORDER BY u.date_of_creation DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<User> findAllUsersPaginated(@Param("limit") int limit,
                                     @Param("cursor") Instant cursor);

    @Query(value = "SELECT COUNT(id) FROM users WHERE is_deleted = false AND is_enabled = true", nativeQuery = true)
    long countAllActiveUsers();

    @Query(value = """
            SELECT u.*
            FROM users u
            WHERE u.is_deleted = false
              AND u.is_enabled = true
              AND u.date_of_creation < :cursor
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
            ORDER BY u.date_of_creation DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<User> searchUsersPaginated(@Param("query") String query,
                                    @Param("rawQuery") String rawQuery,
                                    @Param("limit") int limit,
                                    @Param("cursor") Instant cursor);

    // ─── PROJECTS (paginated search) ─────────────────────────────────────────

    @Query(value = """
            SELECT p.*
            FROM projects p
            WHERE p.is_deleted = false
              AND p.visibility = 'PUBLIC'
              AND p.created_at < :cursor
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
            ORDER BY p.created_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Project> searchProjectsPaginated(@Param("query") String query,
                                          @Param("rawQuery") String rawQuery,
                                          @Param("limit") int limit,
                                          @Param("cursor") Instant cursor);

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
    long countSearchProjectsPaginated(@Param("query") String query,
                                      @Param("rawQuery") String rawQuery);


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

    // ─── PROJECTS ────────────────────────────────────────────────────────────

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

    // ─── FALLBACK: projects by person name (owner or member) ─────────────────
    // Called when the primary project text search returns no results.
    // Joins project_members so we catch both project owners and added members.

    @Query(value = """
            SELECT DISTINCT p.*
            FROM projects p
            LEFT JOIN project_members pm ON pm.project_id = p.id AND pm.is_deleted = false
            LEFT JOIN users u ON u.id = p.owner_id OR u.id = pm.user_id
            WHERE p.is_deleted = false
              AND p.visibility = 'PUBLIC'
              AND (
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(CONCAT(u.first_name, ' ', COALESCE(u.middle_name, ''), ' ', u.last_name))
                        ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY p.created_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Project> searchProjectsByPersonName(@Param("query") String query,
                                             @Param("rawQuery") String rawQuery,
                                             @Param("limit") int limit);

    @Query(value = """
            SELECT COUNT(DISTINCT p.id)
            FROM projects p
            LEFT JOIN project_members pm ON pm.project_id = p.id AND pm.is_deleted = false
            LEFT JOIN users u ON u.id = p.owner_id OR u.id = pm.user_id
            WHERE p.is_deleted = false
              AND p.visibility = 'PUBLIC'
              AND (
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(CONCAT(u.first_name, ' ', COALESCE(u.middle_name, ''), ' ', u.last_name))
                        ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            """,
            nativeQuery = true)
    long countSearchProjectsByPersonName(@Param("query") String query,
                                         @Param("rawQuery") String rawQuery);

    // ─── FALLBACK: projects by person name (paginated, cursor-based) ──────────

    @Query(value = """
            SELECT DISTINCT p.*
            FROM projects p
            LEFT JOIN project_members pm ON pm.project_id = p.id AND pm.is_deleted = false
            LEFT JOIN users u ON u.id = p.owner_id OR u.id = pm.user_id
            WHERE p.is_deleted = false
              AND p.visibility = 'PUBLIC'
              AND p.created_at < :cursor
              AND (
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(CONCAT(u.first_name, ' ', COALESCE(u.middle_name, ''), ' ', u.last_name))
                        ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY p.created_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Project> searchProjectsByPersonNamePaginated(@Param("query") String query,
                                                      @Param("rawQuery") String rawQuery,
                                                      @Param("limit") int limit,
                                                      @Param("cursor") Instant cursor);


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

    // ─── FALLBACK: posts by person name (creator) ─────────────────────────────
    // Called when the primary post content search returns no results.

    @Query(value = """
            SELECT po.*
            FROM posts po
            JOIN users u ON u.id = po.creator_user_id
            WHERE po.is_deleted = false
              AND (
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(CONCAT(u.first_name, ' ', COALESCE(u.middle_name, ''), ' ', u.last_name))
                        ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY po.created_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Post> searchPostsByPersonName(@Param("query") String query,
                                       @Param("rawQuery") String rawQuery,
                                       @Param("limit") int limit);

    @Query(value = """
            SELECT COUNT(po.id)
            FROM posts po
            JOIN users u ON u.id = po.creator_user_id
            WHERE po.is_deleted = false
              AND (
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(CONCAT(u.first_name, ' ', COALESCE(u.middle_name, ''), ' ', u.last_name))
                        ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            """,
            nativeQuery = true)
    long countSearchPostsByPersonName(@Param("query") String query,
                                      @Param("rawQuery") String rawQuery);
    // ─── POSTS (paginated search) ──────────────────────────────────────────────

    @Query(value = """
            SELECT po.*
            FROM posts po
            WHERE po.is_deleted = false
              AND po.created_at < :cursor
              AND (
                    to_tsvector('english', COALESCE(po.content, ''))
                        @@ plainto_tsquery('english', :query)
                    OR LOWER(po.content) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY po.created_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Post> searchPostsPaginated(@Param("query") String query,
                                    @Param("rawQuery") String rawQuery,
                                    @Param("limit") int limit,
                                    @Param("cursor") Instant cursor);

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
    long countSearchPostsPaginated(@Param("query") String query,
                                   @Param("rawQuery") String rawQuery);

    // ─── FALLBACK: posts by person name (paginated) ──────────────────────────

    @Query(value = """
            SELECT po.*
            FROM posts po
            JOIN users u ON u.id = po.creator_user_id
            WHERE po.is_deleted = false
              AND po.created_at < :cursor
              AND (
                    to_tsvector('english',
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.middle_name, '') || ' ' ||
                        COALESCE(u.last_name, '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(CONCAT(u.first_name, ' ', COALESCE(u.middle_name, ''), ' ', u.last_name))
                        ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY po.created_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Post> searchPostsByPersonNamePaginated(@Param("query") String query,
                                                @Param("rawQuery") String rawQuery,
                                                @Param("limit") int limit,
                                                @Param("cursor") Instant cursor);

    // ─── RANDOM SAMPLES ──────────────────────────────────────────────────────

    @Query(value = "SELECT * FROM users WHERE is_deleted = false AND is_enabled = true ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<User> findRandomUsers(@Param("limit") int limit);

    @Query(value = "SELECT * FROM projects WHERE is_deleted = false AND visibility = 'PUBLIC' ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Project> findRandomProjects(@Param("limit") int limit);

    @Query(value = "SELECT * FROM posts WHERE is_deleted = false ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Post> findRandomPosts(@Param("limit") int limit);
}
