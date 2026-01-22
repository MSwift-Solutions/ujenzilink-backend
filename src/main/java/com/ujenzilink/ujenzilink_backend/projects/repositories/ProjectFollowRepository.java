package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectFollowRepository extends JpaRepository<ProjectFollow, UUID> {

    Optional<ProjectFollow> findByProjectAndUser(Project project, User user);

    List<ProjectFollow> findByProject(Project project);

    List<ProjectFollow> findByUser(User user);

    boolean existsByProjectAndUser(Project project, User user);

    long countByProject(Project project);

    List<ProjectFollow> findByProjectAndIsActiveTrue(Project project);

    List<ProjectFollow> findByUserAndIsActiveTrue(User user);
}
