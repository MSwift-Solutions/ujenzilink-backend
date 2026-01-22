package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectFollowDTO;
import com.ujenzilink.ujenzilink_backend.projects.services.UserMgtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
public class UserMgt {

    private final UserMgtService projectUserMgtService;

    public UserMgt(UserMgtService projectUserMgtService) {
        this.projectUserMgtService = projectUserMgtService;
    }

    @PostMapping("/{projectId}/follow")
    public ResponseEntity<ApiCustomResponse<String>> followProject(@PathVariable UUID projectId) {
        ApiCustomResponse<String> response = projectUserMgtService.followProject(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/follows")
    public ResponseEntity<ApiCustomResponse<List<ProjectFollowDTO>>> getProjectFollows(@PathVariable UUID projectId) {
        ApiCustomResponse<List<ProjectFollowDTO>> response = projectUserMgtService.getProjectFollows(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/follow-status")
    public ResponseEntity<ApiCustomResponse<Boolean>> checkFollowStatus(@PathVariable UUID projectId) {
        ApiCustomResponse<Boolean> response = projectUserMgtService.checkFollowStatus(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
