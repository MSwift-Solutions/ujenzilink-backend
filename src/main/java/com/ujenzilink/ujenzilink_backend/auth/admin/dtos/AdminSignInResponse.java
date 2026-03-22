package com.ujenzilink.ujenzilink_backend.auth.admin.dtos;

public record AdminSignInResponse(
        String jwt,
        String name,
        String email,
        String role
) {}
