package com.ujenzilink.ujenzilink_backend.projects.dtos;

import java.util.List;

public record ProjectDropdownsResponse(
                List<DropdownResponse> types,
                List<DropdownResponse> statuses,
                List<DropdownResponse> visibilities,
                List<DropdownResponse> budgetVisibilities,
                List<DropdownResponse> constructionStages,
                List<DropdownResponse> postTypes) {
}
