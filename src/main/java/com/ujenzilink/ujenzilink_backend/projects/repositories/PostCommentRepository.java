package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.PostComment;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    List<PostComment> findByStageAndIsDeletedFalseAndParentCommentIsNullOrderByCreatedAtAsc(ProjectStage stage);

    List<PostComment> findByStageAndIsDeletedFalseOrderByCreatedAtAsc(ProjectStage stage);

    long countByStageAndIsDeletedFalse(ProjectStage stage);
}
