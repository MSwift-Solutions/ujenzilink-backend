package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

public record BackupFailureReportDTO(
    String hostname,
    String timestamp,
    String backup_file,
    String global_roles_file,
    String step,
    String error_message,
    Destination destination,
    Integer keep_hours
) {
    public record Destination(
        String host,
        String dir
    ) {}
}
