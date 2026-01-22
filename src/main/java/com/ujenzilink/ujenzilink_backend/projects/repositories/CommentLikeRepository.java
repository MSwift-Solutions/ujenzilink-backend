package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.models.CommentLike;
import com.ujenzilink.ujenzilink_backend.projects.models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    Optional<CommentLike> findByCommentAndUser(PostComment comment, User user);

    boolean existsByCommentAndUser(PostComment comment, User user);

    long countByComment(PostComment comment);
}
