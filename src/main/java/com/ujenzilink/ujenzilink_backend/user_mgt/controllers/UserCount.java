package com.ujenzilink.ujenzilink_backend.user_mgt.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserCountResponseDto;
import com.ujenzilink.ujenzilink_backend.user_mgt.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/public")
public class UserCount {

    private final UserService userService;

    public UserCount(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user-count")
    public ResponseEntity<ApiCustomResponse<UserCountResponseDto>> getUserCount() {
        ApiCustomResponse<UserCountResponseDto> response = userService.getUserCount();
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
