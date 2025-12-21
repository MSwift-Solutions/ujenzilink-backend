package com.ujenzilink.ujenzilink_backend.auth.contollers;

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

    @PostMapping("/confirm")
    public ResponseEntity<ApiCustomResponse<String>> confirmUser(
            @RequestParam(required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiCustomResponse<>(
                    null,
                    "Token is required",
                    HttpStatus.BAD_REQUEST.value()
            ));
        }

        ApiCustomResponse<String> response = signUpService.confirmToken(token);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("test");
    }
}
