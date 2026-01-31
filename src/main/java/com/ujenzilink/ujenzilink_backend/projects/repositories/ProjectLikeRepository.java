package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectLikeRepository extends JpaRepository<ProjectLike, UUID> {

    Optional<ProjectLike> findByProjectAndUser(Project project, User user);

    List<ProjectLike> findByProject(Project project);

    boolean existsByProjectAndUser(Project project, User user);

    long countByProject(Project project);

}
