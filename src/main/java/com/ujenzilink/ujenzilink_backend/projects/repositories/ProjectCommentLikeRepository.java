package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectCommentLike;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectCommentLikeRepository extends JpaRepository<ProjectCommentLike, UUID> {

    Optional<ProjectCommentLike> findByCommentAndUser(ProjectComment comment, User user);

    boolean existsByCommentAndUser(ProjectComment comment, User user);

    long countByComment(ProjectComment comment);
}
