package com.ujenzilink.ujenzilink_backend.auth.contollers;

import com.ujenzilink.ujenzilink_backend.auth.dtos.SignInResponse;
import com.ujenzilink.ujenzilink_backend.auth.dtos.SignUpRequest;
import com.ujenzilink.ujenzilink_backend.auth.services.SignUpService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class SignUp {

    private final SignUpService signUpService;

    public SignUp(SignUpService signUpService) {
        this.signUpService = signUpService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiCustomResponse<String>> createUser(
            @RequestBody @Valid SignUpRequest signUpRequest,
            @RequestParam(defaultValue = "false") boolean agree) {
        ApiCustomResponse<String> response = signUpService.createUser(signUpRequest, agree);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/confirm")
    public ResponseEntity<ApiCustomResponse<SignInResponse>> confirmUser(
            @RequestParam(required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                    null,
                    "Token required",
                    HttpStatus.BAD_REQUEST.value()));
        }

        ApiCustomResponse<SignInResponse> response = signUpService.confirmToken(token);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiCustomResponse<String>> resendVerification(
            @RequestParam(required = false) String email) {

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                    null,
                    "Email required",
                    HttpStatus.BAD_REQUEST.value()));
        }

        ApiCustomResponse<String> response = signUpService.resendVerification(email);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiCustomResponse<Boolean>> checkUsernameAvailability(
            @RequestParam(required = false) String username) {

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                    false,
                    "Username parameter is required",
                    HttpStatus.BAD_REQUEST.value()));
        }

        boolean isAvailable = !signUpService.isUsernameTaken(username.toLowerCase());

        String message = isAvailable
                ? "Username is available"
                : "Username is already taken";

        return ResponseEntity.ok(new ApiCustomResponse<>(
                isAvailable,
                message,
                HttpStatus.OK.value()));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiCustomResponse<Boolean>> checkEmailAvailability(
            @RequestParam(required = false) String email) {

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                    false,
                    "Email parameter is required",
                    HttpStatus.BAD_REQUEST.value()));
        }

        boolean isAvailable = !signUpService.isEmailTaken(email.toLowerCase());

        String message = isAvailable
                ? "Email is available"
                : "Email is already registered";

        return ResponseEntity.ok(new ApiCustomResponse<>(
                isAvailable,
                message,
                HttpStatus.OK.value()));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("test");
    }
}
