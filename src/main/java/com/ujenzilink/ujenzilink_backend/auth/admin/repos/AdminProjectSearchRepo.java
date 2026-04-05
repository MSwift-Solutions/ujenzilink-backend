package com.ujenzilink.ujenzilink_backend.auth.admin.repos;

import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminProjectSearchRepo extends JpaRepository<Project, UUID> {

    @Query(value = """
            SELECT p.*
            FROM projects p
            JOIN users u ON u.id = p.owner_id
            WHERE p.is_deleted = false
              AND p.created_at < :cursor
              AND (
                    to_tsvector('english',
                        COALESCE(p.title, '') || ' ' ||
                        COALESCE(p.description, '') || ' ' ||
                        COALESCE(p.location, '') || ' ' ||
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.last_name, '') || ' ' ||
                        COALESCE(u.email, '') || ' ' ||
                        COALESCE(CAST(p.project_type AS TEXT), '') || ' ' ||
                        COALESCE(CAST(p.project_status AS TEXT), '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(p.title) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(u.first_name) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(u.last_name) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(u.email) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            ORDER BY p.created_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Project> searchProjectsAdminPaginated(@Param("query") String query,
                                               @Param("rawQuery") String rawQuery,
                                               @Param("limit") int limit,
                                               @Param("cursor") Instant cursor);

    @Query(value = """
            SELECT COUNT(p.id)
            FROM projects p
            JOIN users u ON u.id = p.owner_id
            WHERE p.is_deleted = false
              AND (
                    to_tsvector('english',
                        COALESCE(p.title, '') || ' ' ||
                        COALESCE(p.description, '') || ' ' ||
                        COALESCE(p.location, '') || ' ' ||
                        COALESCE(u.first_name, '') || ' ' ||
                        COALESCE(u.last_name, '') || ' ' ||
                        COALESCE(u.email, '') || ' ' ||
                        COALESCE(CAST(p.project_type AS TEXT), '') || ' ' ||
                        COALESCE(CAST(p.project_status AS TEXT), '')
                    ) @@ plainto_tsquery('english', :query)
                    OR LOWER(p.title) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(u.first_name) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(u.last_name) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                    OR LOWER(u.email) ILIKE LOWER(CONCAT('%', :rawQuery, '%'))
                  )
            """,
            nativeQuery = true)
    long countSearchProjectsAdminPaginated(@Param("query") String query,
                                           @Param("rawQuery") String rawQuery);
}
