package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    List<ProjectMember> findByProject(Project project);

    List<ProjectMember> findByProject_Id(UUID projectId);

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    List<ProjectMember> findByUser(User user);

    List<ProjectMember> findByUser_Id(UUID userId);

    boolean existsByProjectAndUser(Project project, User user);
}
