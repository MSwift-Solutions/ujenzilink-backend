package com.ujenzilink.ujenzilink_backend.auth.contollers;

import com.ujenzilink.ujenzilink_backend.auth.dtos.SignInRequest;
import com.ujenzilink.ujenzilink_backend.auth.dtos.SignInResponse;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.services.SignInService;
import com.ujenzilink.ujenzilink_backend.auth.utils.JWTUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@CrossOrigin
public class SignIn {

    private final SignInService signInService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public SignIn(SignInService signInService, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.signInService = signInService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiCustomResponse<SignInResponse>> signIn(@RequestBody @Valid SignInRequest signInRequest) {
        // Track login attempt (for security auditing)
        signInService.trackLoginAttempt(signInRequest.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.email(), signInRequest.password()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Track failed login attempt
            signInService.trackFailedLoginAttempt(signInRequest.email());
            throw e;
        }

        UserDetails userDetails = signInService.loadUserByUsername(signInRequest.email());
        User user = signInService.findUserByEmail(signInRequest.email());

        String jwt = jwtUtil.generateToken(userDetails);

        // Track successful login
        signInService.trackSuccessfulLogin(signInRequest.email());

        SignInResponse signInData = new SignInResponse(jwt, user.getFirstName(), user.getLastName(), user.getEmail(), user.getUserHandle());

        return ResponseEntity.ok(new ApiCustomResponse<>(
                signInData,
                "Login successful.",
                200));
    }
}
