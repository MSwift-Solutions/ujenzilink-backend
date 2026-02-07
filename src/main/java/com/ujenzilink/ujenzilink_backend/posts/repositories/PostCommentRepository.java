package com.ujenzilink.ujenzilink_backend.posts.repositories;

import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    List<PostComment> findByPostAndIsDeletedFalseAndParentCommentIsNullOrderByCreatedAtDesc(Post post);

    List<PostComment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(PostComment parentComment);

    long countByPostAndIsDeletedFalse(Post post);

    List<PostComment> findByPostAndIsDeletedFalseOrderByCreatedAtDesc(Post post);

    List<PostComment> findByPostAndIsDeletedFalseOrderByCreatedAtAsc(Post post);
}
