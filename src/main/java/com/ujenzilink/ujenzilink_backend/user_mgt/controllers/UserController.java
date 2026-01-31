package com.ujenzilink.ujenzilink_backend.user_mgt.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UpdateUserProfileRequest;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserProfileResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserStatsResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserSummaryResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/summary/me")
    public ResponseEntity<ApiCustomResponse<UserSummaryResponse>> getMySummary() {
        ApiCustomResponse<UserSummaryResponse> response = userService.getMySummary();

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/summary/{username}")
    public ResponseEntity<ApiCustomResponse<UserSummaryResponse>> getUserSummary(
            @PathVariable String username) {
        ApiCustomResponse<UserSummaryResponse> response = userService.getUserSummary(username);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiCustomResponse<String>> deleteUser() {
        ApiCustomResponse<String> response = userService.deleteUser();

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/profile/me")
    public ResponseEntity<ApiCustomResponse<UserProfileResponse>> getMyProfile() {
        ApiCustomResponse<UserProfileResponse> response = userService.getMyProfile();

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @PutMapping("/profile/me")
    public ResponseEntity<ApiCustomResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        ApiCustomResponse<UserProfileResponse> response = userService.updateMyProfile(request);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/stats/me")
    public ResponseEntity<ApiCustomResponse<UserStatsResponse>> getMyStats() {
        ApiCustomResponse<UserStatsResponse> response = userService.getMyStats();

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/stats/{username}")
    public ResponseEntity<ApiCustomResponse<UserStatsResponse>> getUserStats(
            @PathVariable String username) {
        ApiCustomResponse<UserStatsResponse> response = userService.getUserStats(username);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }
}
