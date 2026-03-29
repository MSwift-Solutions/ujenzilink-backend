package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.StagePhoto;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StagePhotoRepository extends JpaRepository<StagePhoto, UUID> {
    List<StagePhoto> findByStageOrderByPhotoOrder(ProjectStage stage);

    java.util.Optional<StagePhoto> findFirstByImage(com.ujenzilink.ujenzilink_backend.images.models.Image image);

    List<StagePhoto> findByStage_IdOrderByPhotoOrder(UUID stageId);

    long countByStage(ProjectStage stage);

    long countByStage_Id(UUID stageId);

    long countByStage_Project_Id(UUID projectId);

    List<StagePhoto> findByStage_Project_IdOrderByUploadedAtDesc(UUID projectId);
}
