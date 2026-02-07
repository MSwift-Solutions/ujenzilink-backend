package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectVisibility;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
        List<Project> findByOwner(User owner);

        List<Project> findByOwnerAndIsDeletedFalse(User owner);

        List<Project> findByOwner_IdAndIsDeletedFalse(UUID ownerId);

        List<Project> findByVisibilityAndIsDeletedFalse(ProjectVisibility visibility);

        List<Project> findByCreatedByAndIsDeletedFalse(User createdBy);

        long countByOwner_IdAndIsDeletedFalse(UUID ownerId);

        // Cursor-based pagination methods
        List<Project> findByVisibilityAndIsDeletedFalseAndCreatedAtBefore(
                        ProjectVisibility visibility, Instant cursor, Pageable pageable);

        List<Project> findByVisibilityAndIsDeletedFalse(
                        ProjectVisibility visibility, Pageable pageable);

        List<Project> findByCreatedByAndIsDeletedFalseAndCreatedAtBefore(
                        User createdBy, Instant cursor, Pageable pageable);

        List<Project> findByCreatedByAndIsDeletedFalse(
                        User createdBy, Pageable pageable);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("UPDATE Project p SET p.impressions = p.impressions + 1 WHERE p.id IN :projectIds")
        void incrementImpressionsInBulk(
                        @org.springframework.data.repository.query.Param("projectIds") java.util.Collection<UUID> projectIds);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("UPDATE Project p SET p.views = p.views + 1 WHERE p.id = :projectId")
        void incrementViews(@org.springframework.data.repository.query.Param("projectId") UUID projectId);
}
