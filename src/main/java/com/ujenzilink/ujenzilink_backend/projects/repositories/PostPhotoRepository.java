package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.Post;
import com.ujenzilink.ujenzilink_backend.projects.models.PostPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostPhotoRepository extends JpaRepository<PostPhoto, UUID> {
    List<PostPhoto> findByPostOrderByPhotoOrder(Post post);

    List<PostPhoto> findByPost_IdOrderByPhotoOrder(UUID postId);

    long countByPost(Post post);

    long countByPost_Id(UUID postId);
}
