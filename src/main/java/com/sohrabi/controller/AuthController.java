package com.sohrabi.controller;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "TOKEN OK";
    }

    @GetMapping("/whoami")
    public String whoami(
            @RequestHeader("Authorization")
            String authHeader) {

        return authHeader;
    }

}
