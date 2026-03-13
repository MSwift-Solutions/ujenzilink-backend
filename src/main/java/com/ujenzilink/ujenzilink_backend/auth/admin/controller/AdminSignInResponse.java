package com.ujenzilink.ujenzilink_backend.auth.admin.controller;

public record AdminSignInResponse(
        String jwt,
        String name,
        String email,
        String role
) {}
