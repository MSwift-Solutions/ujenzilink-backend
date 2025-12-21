package com.ujenzilink.ujenzilink_backend.auth.dtos;

public record SignInResponse(
        String jwt,
        String firstName,
        String lastName
) {}
