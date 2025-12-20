package com.ujenzilink.ujenzilink_backend.configs;

public record ApiCustomResponse<T>(T payload, String message, int statusCode) {}

