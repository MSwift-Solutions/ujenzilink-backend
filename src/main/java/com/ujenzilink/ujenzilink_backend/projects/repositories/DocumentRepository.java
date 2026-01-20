package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.enums.DocumentCategory;
import com.ujenzilink.ujenzilink_backend.projects.models.Document;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByProjectOrderByUploadedAtDesc(Project project);

    List<Document> findByProject_IdOrderByUploadedAtDesc(UUID projectId);

    List<Document> findByStageOrderByUploadedAtDesc(ProjectStage stage);

    List<Document> findByStage_IdOrderByUploadedAtDesc(UUID stageId);

    List<Document> findByCategoryAndProjectOrderByUploadedAtDesc(DocumentCategory category, Project project);

    List<Document> findByCategoryAndProject_IdOrderByUploadedAtDesc(DocumentCategory category, UUID projectId);
}
