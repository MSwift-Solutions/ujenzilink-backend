package com.ujenzilink.ujenzilink_backend.legal.dtos;

import jakarta.validation.constraints.NotBlank;

public record UpdateLegalDocRequest(
        @NotBlank(message = "Content cannot be blank")
        String content
) {}
