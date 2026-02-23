package com.malcolm.ecomproj.controller;

import com.malcolm.ecomproj.dto.AuthenticationResponse;
import com.malcolm.ecomproj.dto.LoginRequest;
import com.malcolm.ecomproj.dto.RegisterRequest;
import com.malcolm.ecomproj.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }
}
