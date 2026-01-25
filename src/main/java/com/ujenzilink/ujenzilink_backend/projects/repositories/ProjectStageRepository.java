package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectStageRepository extends JpaRepository<ProjectStage, UUID> {
    List<ProjectStage> findByProjectOrderByCreatedAtAsc(Project project);

    List<ProjectStage> findByProject_IdOrderByCreatedAtAsc(UUID projectId);

    List<ProjectStage> findByProject(Project project);

    List<ProjectStage> findTop3ByProject_IdOrderByCreatedAtDesc(UUID projectId);
}
