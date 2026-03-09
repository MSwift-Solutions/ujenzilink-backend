package com.ujenzilink.ujenzilink_backend.search.dtos;

import java.util.List;

public record PeoplePageResponse(
        List<PersonDTO> people,
        String nextCursor,
        boolean hasMore) {
}
