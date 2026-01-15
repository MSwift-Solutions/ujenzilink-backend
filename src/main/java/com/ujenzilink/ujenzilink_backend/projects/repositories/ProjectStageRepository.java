package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectStageRepository extends JpaRepository<ProjectStage, UUID> {
    List<ProjectStage> findByProjectOrderByStageOrder(Project project);

    List<ProjectStage> findByProject_IdOrderByStageOrder(UUID projectId);

    List<ProjectStage> findByProject(Project project);
}
