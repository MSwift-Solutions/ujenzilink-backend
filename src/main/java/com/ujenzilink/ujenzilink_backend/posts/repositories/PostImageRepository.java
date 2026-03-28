package com.ujenzilink.ujenzilink_backend.posts.repositories;

import com.ujenzilink.ujenzilink_backend.posts.models.Post;
import com.ujenzilink.ujenzilink_backend.posts.models.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, UUID> {

    List<PostImage> findByPostOrderByImageOrderAsc(Post post);

    java.util.Optional<PostImage> findFirstByImage(com.ujenzilink.ujenzilink_backend.images.models.Image image);

    long countByPost(Post post);

    void deleteByPost(Post post);
}
