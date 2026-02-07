package com.ujenzilink.ujenzilink_backend.posts.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.posts.models.PostComment;
import com.ujenzilink.ujenzilink_backend.posts.models.PostCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostCommentLikeRepository extends JpaRepository<PostCommentLike, UUID> {

    Optional<PostCommentLike> findByCommentAndUser(PostComment comment, User user);

    boolean existsByCommentAndUser(PostComment comment, User user);

    long countByComment(PostComment comment);

    void deleteByCommentAndUser(PostComment comment, User user);
}
