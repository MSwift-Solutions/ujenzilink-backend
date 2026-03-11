package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CommentDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateCommentRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.AddMemberRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectFollowDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectLikeDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectMemberDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.TeamMemberSearchDTO;
import com.ujenzilink.ujenzilink_backend.projects.services.ProjectCommentService;
import com.ujenzilink.ujenzilink_backend.projects.services.UserMgtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
public class UserMgt {

    private final UserMgtService projectUserMgtService;
    private final ProjectCommentService projectCommentService;

    public UserMgt(UserMgtService projectUserMgtService, ProjectCommentService projectCommentService) {
        this.projectUserMgtService = projectUserMgtService;
        this.projectCommentService = projectCommentService;
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

    @PostMapping("/{projectId}/like")
    public ResponseEntity<ApiCustomResponse<String>> likeProject(@PathVariable UUID projectId) {
        ApiCustomResponse<String> response = projectUserMgtService.likeProject(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/like-status")
    public ResponseEntity<ApiCustomResponse<Boolean>> checkLikeStatus(@PathVariable UUID projectId) {
        ApiCustomResponse<Boolean> response = projectUserMgtService.checkLikeStatus(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/likes")
    public ResponseEntity<ApiCustomResponse<List<ProjectLikeDTO>>> getProjectLikes(@PathVariable UUID projectId) {
        ApiCustomResponse<List<ProjectLikeDTO>> response = projectUserMgtService.getProjectLikes(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/comments")
    public ResponseEntity<ApiCustomResponse<List<CommentDTO>>> getProjectComments(@PathVariable UUID projectId) {
        ApiCustomResponse<List<CommentDTO>> response = projectCommentService.getProjectComments(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/{projectId}/comments")
    public ResponseEntity<ApiCustomResponse<CommentDTO>> createProjectComment(
            @PathVariable UUID projectId,
            @RequestBody CreateCommentRequest request) {
        ApiCustomResponse<CommentDTO> response = projectCommentService.createComment(projectId, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/{projectId}/comments/{commentId}/like")
    public ResponseEntity<ApiCustomResponse<String>> likeProjectComment(
            @PathVariable UUID projectId,
            @PathVariable UUID commentId) {
        ApiCustomResponse<String> response = projectCommentService.likeComment(commentId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/search-members")
    public ResponseEntity<ApiCustomResponse<List<TeamMemberSearchDTO>>> searchTeamMembers(
            @RequestParam String query) {
        ApiCustomResponse<List<TeamMemberSearchDTO>> response = projectUserMgtService.searchTeamMembers(query);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiCustomResponse<String>> addMember(
            @PathVariable UUID projectId,
            @RequestBody AddMemberRequest request) {
        ApiCustomResponse<String> response = projectUserMgtService.addMember(projectId, request.userId(),
                request.role());
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<ApiCustomResponse<List<ProjectMemberDTO>>> getProjectMembers(@PathVariable UUID projectId) {
        ApiCustomResponse<List<ProjectMemberDTO>> response = projectUserMgtService.getProjectMembers(projectId);
        return ResponseEntity.status(response.statusCode()).body(response);

    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiCustomResponse<String>> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        ApiCustomResponse<String> response = projectUserMgtService.removeMember(projectId, userId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
