package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.StageLike;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StageLikeRepository extends JpaRepository<StageLike, UUID> {

    List<StageLike> findByStage(ProjectStage stage);

    long countByStage(ProjectStage stage);

}
