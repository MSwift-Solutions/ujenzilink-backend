package com.ujenzilink.ujenzilink_backend.auth.contollers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class Test {

    @GetMapping
    public ResponseEntity<String> test() {
        String body = "{\"status\":\"success\", \"message\":\"UjenziLink Spring Boot is running!\"}";
        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body);
    }
}
