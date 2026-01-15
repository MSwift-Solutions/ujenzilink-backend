package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.models.Post;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByProjectAndIsDeletedFalseOrderByCreatedAtDesc(Project project);

    List<Post> findByProject_IdAndIsDeletedFalseOrderByCreatedAtDesc(UUID projectId);

    List<Post> findByStageAndIsDeletedFalseOrderByCreatedAtDesc(ProjectStage stage);

    List<Post> findByStage_IdAndIsDeletedFalseOrderByCreatedAtDesc(UUID stageId);

    List<Post> findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(User author);

    List<Post> findByAuthor_IdAndIsDeletedFalseOrderByCreatedAtDesc(UUID authorId);
}
