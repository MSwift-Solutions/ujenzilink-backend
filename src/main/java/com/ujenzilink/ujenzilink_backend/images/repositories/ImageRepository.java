package com.ujenzilink.ujenzilink_backend.images.repositories;

import com.ujenzilink.ujenzilink_backend.images.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}

