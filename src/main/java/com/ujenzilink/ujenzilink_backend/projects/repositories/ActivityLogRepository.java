package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.ActivityLog;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findByProjectOrderByTimestampDesc(Project project);

    List<ActivityLog> findByProject_IdOrderByTimestampDesc(UUID projectId);

    List<ActivityLog> findTop20ByProjectOrderByTimestampDesc(Project project);

    List<ActivityLog> findTop20ByProject_IdOrderByTimestampDesc(UUID projectId);

    List<ActivityLog> findTop50ByProjectOrderByTimestampDesc(Project project);
}
