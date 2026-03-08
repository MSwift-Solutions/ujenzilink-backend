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

    // ─── POSTS ───────────────────────────────────────────────────────────────

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
}
