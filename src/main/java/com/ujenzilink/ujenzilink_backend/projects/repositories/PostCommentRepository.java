package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.Post;
import com.ujenzilink.ujenzilink_backend.projects.models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
    List<PostComment> findByPostAndIsDeletedFalseOrderByCreatedAtAsc(Post post);

    List<PostComment> findByPost_IdAndIsDeletedFalseOrderByCreatedAtAsc(UUID postId);

    long countByPostAndIsDeletedFalse(Post post);
}
