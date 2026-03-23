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

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p JOIN FETCH p.creator c LEFT JOIN FETCH c.profilePicture WHERE p.isDeleted = false AND p.createdAt < :cursor")
    List<Post> findByIsDeletedFalseAndCreatedAtBeforeWithCreator(
            @org.springframework.data.repository.query.Param("cursor") Instant cursor,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p JOIN FETCH p.creator c LEFT JOIN FETCH c.profilePicture WHERE p.isDeleted = false")
    List<Post> findByIsDeletedFalseWithCreator(Pageable pageable);

    List<Post> findByIsDeletedFalse(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p JOIN FETCH p.creator c LEFT JOIN FETCH c.profilePicture WHERE p.creator = :creator AND p.isDeleted = false AND p.createdAt < :cursor")
    List<Post> findByCreatorAndIsDeletedFalseAndCreatedAtBeforeWithCreator(
            @org.springframework.data.repository.query.Param("creator") User creator,
            @org.springframework.data.repository.query.Param("cursor") Instant cursor,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p JOIN FETCH p.creator c LEFT JOIN FETCH c.profilePicture WHERE p.creator = :creator AND p.isDeleted = false")
    List<Post> findByCreatorAndIsDeletedFalseWithCreator(
            @org.springframework.data.repository.query.Param("creator") User creator,
            Pageable pageable);

    List<Post> findByCreatorAndIsDeletedFalse(User creator, Pageable pageable);

    long countByCreator_IdAndIsDeletedFalse(UUID creatorId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Post p SET p.impressions = p.impressions + 1 WHERE p.id IN :postIds")
    void incrementImpressionsInBulk(
            @org.springframework.data.repository.query.Param("postIds") java.util.Collection<UUID> postIds);

    long countByIsDeletedFalse();
}
