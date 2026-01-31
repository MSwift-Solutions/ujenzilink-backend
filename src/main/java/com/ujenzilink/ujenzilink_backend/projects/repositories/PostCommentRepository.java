package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.PostComment;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    List<PostComment> findByProjectAndIsDeletedFalseAndParentCommentIsNullOrderByCreatedAtAsc(Project project);

    List<PostComment> findByProjectAndIsDeletedFalseOrderByCreatedAtAsc(Project project);

    long countByProjectAndIsDeletedFalse(Project project);

    long countByCommenter_IdAndIsDeletedFalse(UUID commenterId);
}
