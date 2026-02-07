package com.ujenzilink.ujenzilink_backend.posts.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    Optional<PostLike> findByPostAndUserAndIsDeletedFalse(Post post, User user);

    boolean existsByPostAndUserAndIsDeletedFalse(Post post, User user);

    long countByPostAndIsDeletedFalse(Post post);

    List<PostLike> findByPostAndIsDeletedFalse(Post post);
}
