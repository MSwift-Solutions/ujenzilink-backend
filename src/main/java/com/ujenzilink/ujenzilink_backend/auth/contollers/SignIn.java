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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.email(), signInRequest.password())
        );

        System.out.println("reacheeeeeeeeeeeeeee");

        UserDetails userDetails = signInService.loadUserByUsername(signInRequest.email());
        User user = signInService.findUserByEmail(signInRequest.email());

        String jwt = jwtUtil.generateToken(userDetails);

        SignInResponse signInData = new SignInResponse(jwt, user.getFirstName(), user.getLastName());

        return ResponseEntity.ok(new ApiCustomResponse<>(
                signInData,
                "Sign in successful",
                200
        ));
    }
}
