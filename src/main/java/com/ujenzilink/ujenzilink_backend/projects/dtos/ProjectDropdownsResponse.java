package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.util.List;

public record ProjectDropdownsResponse(
        List<DropdownResponse> projectTypes,
        List<DropdownResponse> projectStatuses,
        List<DropdownResponse> projectVisibilities,
        List<DropdownResponse> budgetVisibilities) {
}
