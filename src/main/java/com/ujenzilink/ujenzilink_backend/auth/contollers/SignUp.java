package com.ujenzilink.ujenzilink_backend.auth.contollers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class SignUp {

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
