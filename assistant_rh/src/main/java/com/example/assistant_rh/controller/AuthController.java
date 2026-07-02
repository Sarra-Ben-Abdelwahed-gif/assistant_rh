package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.request.CandidateRegisterRequest;
import com.example.assistant_rh.dto.request.LoginRequest;
import com.example.assistant_rh.dto.request.RefreshTokenRequest;
import com.example.assistant_rh.dto.request.RegisterRequest;
import com.example.assistant_rh.dto.request.UpdateProfileRequest;
import com.example.assistant_rh.dto.response.AuthResponse;
import com.example.assistant_rh.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification",
    description = "Login, registration, profile")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Connexion")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody
            LoginRequest request) {
        return ResponseEntity.ok(
            authService.login(request));
    }

    @Operation(summary = "Create an employee")
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody
            RegisterRequest request) {
        return ResponseEntity.ok(
            authService.register(request));
    }

    @Operation(summary = "Candidate registration")
    @PostMapping("/register/candidate")
    public ResponseEntity<AuthResponse>
            registerCandidate(
            @Valid @RequestBody
            CandidateRegisterRequest request) {
        return ResponseEntity.ok(
            authService.registerCandidate(request));
    }

    @Operation(summary = "Edit my profile")
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse>
            updateProfile(
            @Valid @RequestBody
            UpdateProfileRequest request) {
        return ResponseEntity.ok(
            authService.updateProfile(request));
    }

    @Operation(summary = "Refresh Token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody
            RefreshTokenRequest request) {
        
        return ResponseEntity.ok(
            authService.refreshToken(
                request.getRefreshToken()));
    }

    @Operation(summary = "Disconnect")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody
            RefreshTokenRequest request) {
        authService.logout(
            request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}