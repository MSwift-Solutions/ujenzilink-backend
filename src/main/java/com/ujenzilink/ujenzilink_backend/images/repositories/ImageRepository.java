package com.ujenzilink.ujenzilink_backend.images.repositories;

import com.ujenzilink.ujenzilink_backend.images.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    java.util.Optional<Image> findByUrl(String url);
}

