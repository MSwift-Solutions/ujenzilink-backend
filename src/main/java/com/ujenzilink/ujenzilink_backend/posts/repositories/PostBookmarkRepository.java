package com.ujenzilink.ujenzilink_backend.posts.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, UUID> {

    Optional<PostBookmark> findByPostAndUserAndIsDeletedFalse(Post post, User user);

    boolean existsByPostAndUserAndIsDeletedFalse(Post post, User user);

    List<PostBookmark> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user);

    long countByPostAndIsDeletedFalse(Post post);

    List<PostBookmark> findByPostAndIsDeletedFalse(Post post);
}
