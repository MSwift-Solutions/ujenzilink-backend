package com.ujenzilink.ujenzilink_backend.projects.dtos;

import com.ujenzilink.ujenzilink_backend.projects.enums.PlanFileFormat;
import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlanFile;

import java.time.Instant;
import java.util.UUID;

public record PlanFileResponse(
        UUID id,
        UUID planId,
        String fileName,
        String fileUrl,
        Long fileSize,
        PlanFileFormat format,
        String displayLabel,
        String version,
        Instant uploadedAt
) {
    public static PlanFileResponse from(ProjectPlanFile file, String r2PublicUrl) {
        return new PlanFileResponse(
                file.getId(),
                file.getPlan().getId(),
                file.getFileName(),
                r2PublicUrl + "/" + file.getFileStorageKey(),
                file.getFileSize(),
                file.getFormat(),
                file.getDisplayLabel(),
                file.getVersion(),
                file.getUploadedAt()
        );
    }
}
