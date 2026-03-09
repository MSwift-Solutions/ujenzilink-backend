package com.ujenzilink.ujenzilink_backend.search.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.search.dtos.PeoplePageResponse;
import com.ujenzilink.ujenzilink_backend.search.dtos.PostSearchPageResponse;
import com.ujenzilink.ujenzilink_backend.search.dtos.ProjectSearchPageResponse;
import com.ujenzilink.ujenzilink_backend.search.dtos.SearchResponse;
import com.ujenzilink.ujenzilink_backend.search.services.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }


    @GetMapping
    public ResponseEntity<ApiCustomResponse<SearchResponse>> search(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {

        ApiCustomResponse<SearchResponse> response = searchService.search(q, limit);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/sample")
    public ResponseEntity<ApiCustomResponse<SearchResponse>> getSampleData() {
        ApiCustomResponse<SearchResponse> response = searchService.getSampleData();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/people")
    public ResponseEntity<ApiCustomResponse<PeoplePageResponse>> searchPeople(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {

        ApiCustomResponse<PeoplePageResponse> response = searchService.searchPeoplePaginated(q, cursor, limit);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiCustomResponse<ProjectSearchPageResponse>> searchProjects(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {

        ApiCustomResponse<ProjectSearchPageResponse> response = searchService.searchProjectsPaginated(q, cursor, limit);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiCustomResponse<PostSearchPageResponse>> searchPosts(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {

        ApiCustomResponse<PostSearchPageResponse> response = searchService.searchPostsPaginated(q, cursor, limit);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
