package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectPlanRepository extends JpaRepository<ProjectPlan, UUID> {
    List<ProjectPlan> findByProject_IdAndIsDeletedFalseOrderByCreatedAtDesc(UUID projectId);
}
