package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.ProjectComment;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectCommentRepository extends JpaRepository<ProjectComment, UUID> {

    List<ProjectComment> findByProjectAndIsDeletedFalseAndParentCommentIsNullOrderByCreatedAtAsc(Project project);

    List<ProjectComment> findByProjectAndIsDeletedFalseOrderByCreatedAtAsc(Project project);

    long countByProjectAndIsDeletedFalse(Project project);

}
