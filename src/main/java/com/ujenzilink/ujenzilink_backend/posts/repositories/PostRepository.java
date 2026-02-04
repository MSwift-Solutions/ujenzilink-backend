package com.ujenzilink.ujenzilink_backend.posts.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByCreator_IdAndIsDeletedFalse(UUID creatorId);

    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    List<Post> findByCreatorAndIsDeletedFalse(User creator);

    List<Post> findByIsDeletedFalseAndCreatedAtBefore(Instant cursor, Pageable pageable);

    List<Post> findByIsDeletedFalse(Pageable pageable);

    List<Post> findByCreatorAndIsDeletedFalseAndCreatedAtBefore(User creator, Instant cursor, Pageable pageable);

    List<Post> findByCreatorAndIsDeletedFalse(User creator, Pageable pageable);

    long countByCreator_IdAndIsDeletedFalse(UUID creatorId);
}
