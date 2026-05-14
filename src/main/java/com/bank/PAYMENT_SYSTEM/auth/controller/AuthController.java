package com.bank.PAYMENT_SYSTEM.auth.controller;


import com.bank.PAYMENT_SYSTEM.auth.dto.AuthResponse;
import com.bank.PAYMENT_SYSTEM.auth.dto.LoginRequest;
import com.bank.PAYMENT_SYSTEM.auth.dto.RegisterRequest;
import com.bank.PAYMENT_SYSTEM.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService) {

        this.authService = authService;
    }

    // REGISTER

    @PostMapping("/register")
    public String register(
            @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    // LOGIN

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}
