package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.enums.DocumentCategory;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectDocument;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectDocumentRepository extends JpaRepository<ProjectDocument, UUID> {
    List<ProjectDocument> findByProjectOrderByUploadedAtDesc(Project project);

    List<ProjectDocument> findByProject_IdOrderByUploadedAtDesc(UUID projectId);

    List<ProjectDocument> findByStageOrderByUploadedAtDesc(ProjectStage stage);

    List<ProjectDocument> findByStage_IdOrderByUploadedAtDesc(UUID stageId);

    List<ProjectDocument> findByCategoryAndProjectOrderByUploadedAtDesc(DocumentCategory category, Project project);

    List<ProjectDocument> findByCategoryAndProject_IdOrderByUploadedAtDesc(DocumentCategory category, UUID projectId);
}
