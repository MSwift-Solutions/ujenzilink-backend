package com.ujenzilink.ujenzilink_backend.search.dtos;

import java.util.List;

public record SearchResponse(
        List<SearchPeopleResult> people,
        List<SearchProjectResult> projects,
        List<SearchPostResult> posts,
        SearchResultCounts counts) {
}
