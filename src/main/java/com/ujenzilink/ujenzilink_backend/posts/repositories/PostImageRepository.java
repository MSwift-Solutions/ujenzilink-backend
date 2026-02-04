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

    long countByPost(Post post);

    void deleteByPost(Post post);
}
