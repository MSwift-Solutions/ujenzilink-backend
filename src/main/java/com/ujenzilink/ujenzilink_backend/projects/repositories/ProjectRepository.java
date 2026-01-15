package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.enums.ProjectVisibility;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByOwner(User owner);

    List<Project> findByOwnerAndIsDeletedFalse(User owner);

    List<Project> findByOwner_IdAndIsDeletedFalse(UUID ownerId);

    List<Project> findByVisibilityAndIsDeletedFalse(ProjectVisibility visibility);

    List<Project> findByCreatedByAndIsDeletedFalse(User createdBy);
}
