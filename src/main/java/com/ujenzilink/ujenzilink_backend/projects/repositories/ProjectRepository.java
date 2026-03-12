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

        List<Project> findByCreatedByAndIsDeletedFalse(
                        User createdBy, Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT p FROM Project p JOIN FETCH p.createdBy c LEFT JOIN FETCH c.profilePicture WHERE " +
                        "(p.createdBy = :user OR EXISTS (SELECT 1 FROM ProjectMember pm WHERE pm.project = p AND pm.user = :user AND pm.isDeleted = false)) " +
                        "AND p.isDeleted = false " +
                        "AND (:cursorTime IS NULL OR p.createdAt < :cursorTime)")
        List<Project> findByUserInvolvedWithCursor(
                        @org.springframework.data.repository.query.Param("user") User user,
                        @org.springframework.data.repository.query.Param("cursorTime") Instant cursorTime,
                        Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT p FROM Project p JOIN FETCH p.createdBy c LEFT JOIN FETCH c.profilePicture WHERE " +
                        "(p.createdBy = :user OR EXISTS (SELECT 1 FROM ProjectMember pm WHERE pm.project = p AND pm.user = :user AND pm.isDeleted = false)) " +
                        "AND p.isDeleted = false")
        List<Project> findByUserInvolved(
                        @org.springframework.data.repository.query.Param("user") User user,
                        Pageable pageable);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("UPDATE Project p SET p.impressions = p.impressions + 1 WHERE p.id IN :projectIds")
        void incrementImpressionsInBulk(
                        @org.springframework.data.repository.query.Param("projectIds") java.util.Collection<UUID> projectIds);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("UPDATE Project p SET p.views = p.views + 1 WHERE p.id = :projectId")
        void incrementViews(@org.springframework.data.repository.query.Param("projectId") UUID projectId);
}
