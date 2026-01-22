package com.ujenzilink.ujenzilink_backend.projects.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CommentDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreateCommentRequest;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectFollowDTO;
import com.ujenzilink.ujenzilink_backend.projects.dtos.ProjectLikeDTO;
import com.ujenzilink.ujenzilink_backend.projects.services.PostCommentService;
import com.ujenzilink.ujenzilink_backend.projects.services.UserMgtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
public class UserMgt {

    private final UserMgtService projectUserMgtService;
    private final PostCommentService postCommentService;

    public UserMgt(UserMgtService projectUserMgtService, PostCommentService postCommentService) {
        this.projectUserMgtService = projectUserMgtService;
        this.postCommentService = postCommentService;
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

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiCustomResponse<List<CommentDTO>>> getPostComments(@PathVariable UUID postId) {
        ApiCustomResponse<List<CommentDTO>> response = postCommentService.getStageComments(postId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiCustomResponse<CommentDTO>> createPostComment(
            @PathVariable UUID postId,
            @RequestBody CreateCommentRequest request) {
        ApiCustomResponse<CommentDTO> response = postCommentService.createComment(postId, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
