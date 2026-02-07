package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.ProjectActivityLog;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectActivityLogRepository extends JpaRepository<ProjectActivityLog, UUID> {
    List<ProjectActivityLog> findByProjectOrderByTimestampDesc(Project project);

    List<ProjectActivityLog> findByProject_IdOrderByTimestampDesc(UUID projectId);

    List<ProjectActivityLog> findTop20ByProjectOrderByTimestampDesc(Project project);

    List<ProjectActivityLog> findTop20ByProject_IdOrderByTimestampDesc(UUID projectId);

    List<ProjectActivityLog> findTop50ByProjectOrderByTimestampDesc(Project project);
}
