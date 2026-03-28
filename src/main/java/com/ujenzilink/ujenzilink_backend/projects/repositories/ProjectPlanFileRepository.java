package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlanFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectPlanFileRepository extends JpaRepository<ProjectPlanFile, UUID> {
    List<ProjectPlanFile> findByPlan_IdAndIsDeletedFalseOrderByUploadedAtDesc(UUID planId);
}
