package com.ujenzilink.ujenzilink_backend.user_mgt.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserSummaryResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiCustomResponse<UserSummaryResponse>> getUserSummary() {
        ApiCustomResponse<UserSummaryResponse> response = userService.getUserSummary();

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
}
