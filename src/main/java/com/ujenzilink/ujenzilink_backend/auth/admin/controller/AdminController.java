package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

import com.ujenzilink.ujenzilink_backend.auth.admin.AdminUser;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuthService;
import com.ujenzilink.ujenzilink_backend.auth.utils.JWTUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/auth")
@CrossOrigin
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public AdminController(AdminAuthService adminAuthService,
                           PasswordEncoder passwordEncoder,
                           JWTUtil jwtUtil) {
        this.adminAuthService = adminAuthService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiCustomResponse<AdminSignInResponse>> signIn(
            @RequestBody @Valid AdminSignInRequest request,
            HttpServletRequest httpRequest) {

        String email = request.email().toLowerCase();
        AdminUser admin;

        try {
            admin = (AdminUser) adminAuthService.loadUserByUsername(email);
            
            if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
                throw new BadCredentialsException("Invalid credentials.");
            }
            
            if (!admin.isEnabled()) {
                throw new BadCredentialsException("Admin account is disabled.");
            }
            
        } catch (Exception e) {
            adminAuthService.recordLoginFailure(email, e.getMessage(), httpRequest);
            throw new BadCredentialsException("Invalid credentials.");
        }

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
