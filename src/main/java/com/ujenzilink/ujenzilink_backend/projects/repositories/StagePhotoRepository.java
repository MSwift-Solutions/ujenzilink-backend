package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.PostPhoto;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StagePhotoRepository extends JpaRepository<PostPhoto, UUID> {
    List<PostPhoto> findByStageOrderByPhotoOrder(ProjectStage stage);

    List<PostPhoto> findByStage_IdOrderByPhotoOrder(UUID stageId);

    long countByStage(ProjectStage stage);

    long countByStage_Id(UUID stageId);

    long countByStage_Project_Id(UUID projectId);
}
