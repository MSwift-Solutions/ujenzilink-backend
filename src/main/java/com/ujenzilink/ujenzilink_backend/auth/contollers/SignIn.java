package com.ujenzilink.ujenzilink_backend.auth.contollers;

import com.ujenzilink.ujenzilink_backend.auth.dtos.SignInRequest;
import com.ujenzilink.ujenzilink_backend.auth.dtos.SignInResponse;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.services.SignInService;
import com.ujenzilink.ujenzilink_backend.auth.utils.JWTUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public ResponseEntity<ApiCustomResponse<SignInResponse>> signIn(@RequestBody SignInRequest signInRequest) {
        System.out.println(signInRequest);

        try {
            UserDetails userDetails = signInService.loadUserByUsername(signInRequest.email());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signInRequest.email(),
                            signInRequest.password()));

            final String jwt = jwtUtil.generateToken(userDetails);
            System.out.println("token " + jwt);

            User user = signInService.findUserByEmail(signInRequest.email());

            SignInResponse signInResponse = new SignInResponse(
                    jwt,
                    user.getFirstName(),
                    user.getLastName()
            );

            ApiCustomResponse<SignInResponse> response = new ApiCustomResponse<>(
                    signInResponse,
                    "Sign in successful",
                    HttpStatus.OK.value()
            );

            return ResponseEntity.status(response.statusCode()).body(response);

        } catch (UsernameNotFoundException e) {
            ApiCustomResponse<SignInResponse> response = new ApiCustomResponse<>(
                    null,
                    e.getMessage(),
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(response.statusCode()).body(response);

        } catch (BadCredentialsException e) {
            ApiCustomResponse<SignInResponse> response = new ApiCustomResponse<>(
                    null,
                    "Invalid password",
                    HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(response.statusCode()).body(response);

        } catch (DisabledException e) {
            ApiCustomResponse<SignInResponse> response = new ApiCustomResponse<>(
                    null,
                    "User is disabled, kindly verify account first before trying to log in. Confirmation code was sent to your email",
                    HttpStatus.FORBIDDEN.value()
            );
            return ResponseEntity.status(response.statusCode()).body(response);

        } catch (Exception e) {
            ApiCustomResponse<SignInResponse> response = new ApiCustomResponse<>(
                    null,
                    "Something went wrong, retry",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(response.statusCode()).body(response);
        }
    }
}