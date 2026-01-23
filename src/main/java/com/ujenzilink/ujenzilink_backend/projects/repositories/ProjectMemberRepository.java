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
    List<ProjectMember> findByProjectAndIsDeletedFalse(Project project);

    List<ProjectMember> findByProject_IdAndIsDeletedFalse(UUID projectId);

    Optional<ProjectMember> findByProjectAndUserAndIsDeletedFalse(Project project, User user);

    List<ProjectMember> findByUserAndIsDeletedFalse(User user);

    List<ProjectMember> findByUser_IdAndIsDeletedFalse(UUID userId);

    boolean existsByProjectAndUserAndIsDeletedFalse(Project project, User user);
}
