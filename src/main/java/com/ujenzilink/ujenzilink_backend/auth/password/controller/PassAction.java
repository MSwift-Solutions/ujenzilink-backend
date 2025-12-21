package com.ujenzilink.ujenzilink_backend.auth.password.controller;

import com.ujenzilink.ujenzilink_backend.auth.password.dto.ForgotPassResetNew;
import com.ujenzilink.ujenzilink_backend.auth.password.dto.ForgotPassResetRequest;
import com.ujenzilink.ujenzilink_backend.auth.password.dto.PassChangeRequest;
import com.ujenzilink.ujenzilink_backend.auth.password.services.PassActionsService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@CrossOrigin
public class PassAction {

    @Autowired
    private PassActionsService passActionsService;

    @PostMapping("/change-password")
    public ResponseEntity<ApiCustomResponse<String>> changePassword(@RequestBody @Valid PassChangeRequest request) {
        ApiCustomResponse<String> response = passActionsService.changePassword(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<ApiCustomResponse<String>> resetPasswordRequest(
            @RequestBody @Valid ForgotPassResetRequest request) {
        ApiCustomResponse<String> response = passActionsService.requestPasswordReset(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiCustomResponse<String>> resetPassword(@RequestBody @Valid ForgotPassResetNew request) {
        ApiCustomResponse<String> response = passActionsService.resetPassword(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
