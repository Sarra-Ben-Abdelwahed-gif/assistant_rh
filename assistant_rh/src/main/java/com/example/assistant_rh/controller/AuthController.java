package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.request.*;
import com.example.assistant_rh.dto.response.AuthResponse;
import com.example.assistant_rh.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
            authService.login(request));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(
            authService.register(request));
    }

    @PostMapping("/register/candidate")
    public ResponseEntity<AuthResponse> registerCandidate(
            @Valid @RequestBody
            CandidateRegisterRequest request) {
        return ResponseEntity.ok(
            authService.registerCandidate(request));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> updateProfile(
            @Valid @RequestBody
            UpdateProfileRequest request) {
        return ResponseEntity.ok(
            authService.updateProfile(request));
    }
}