package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminUser;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuthService;
import com.ujenzilink.ujenzilink_backend.auth.utils.JWTUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/auth")
@CrossOrigin
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public AdminController(AdminAuthService adminAuthService,
                           AuthenticationManager authenticationManager,
                           JWTUtil jwtUtil) {
        this.adminAuthService = adminAuthService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiCustomResponse<AdminSignInResponse>> signIn(
            @RequestBody @Valid AdminSignInRequest request,
            HttpServletRequest httpRequest) {

        String email = request.email().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException e) {
            adminAuthService.recordLoginFailure(email, e.getMessage(), httpRequest);
            throw e;
        }

        AdminUser admin = (AdminUser) adminAuthService.loadUserByUsername(email);
        String jwt = jwtUtil.generateToken(admin);

        adminAuthService.recordLoginSuccess(email, httpRequest);

        AdminSignInResponse response = new AdminSignInResponse(
                jwt,
                admin.getName(),
                admin.getEmail(),
                admin.getRole().name());

        return ResponseEntity.ok(new ApiCustomResponse<>(response, "Admin login successful.", 200));
    }
}
