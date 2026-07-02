package com.example.assistant_rh.service;

import com.example.assistant_rh.dto.request
    .LoginRequest;
import com.example.assistant_rh.dto.request
    .CandidateRegisterRequest;
import com.example.assistant_rh.dto.response
    .AuthResponse;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.enums.Role;
import com.example.assistant_rh.exception
    .EmailAlreadyExistsException;
import com.example.assistant_rh.exception
    .PasswordMismatchException;
import com.example.assistant_rh.repository
    .EmployeeRepository;
import com.example.assistant_rh.repository
    .UserRepository;
import com.example.assistant_rh.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension
    .ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication
    .AuthenticationManager;
import org.springframework.security.crypto.password
    .PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager
        authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .id(1L)
            .email("test@test.com")
            .password("hashedPassword")
            .role(Role.CANDIDATE)
            .build();
    }

    // ── Test login réussi ─────────────────────
    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");

        when(userRepository.findByEmail(
            "test@test.com"))
            .thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(mockUser))
            .thenReturn("mock-token");
        when(refreshTokenService
            .createRefreshToken(mockUser))
            .thenReturn(
                com.example.assistant_rh
                    .entity.RefreshToken.builder()
                    .token("refresh-token")
                    .user(mockUser)
                    .expiresAt(
                        java.time.LocalDateTime
                        .now().plusDays(7))
                    .build());

        AuthResponse response =
            authService.login(req);

        assertNotNull(response);
        assertEquals("test@test.com",
            response.getEmail());
        assertEquals(Role.CANDIDATE,
            response.getRole());
        assertNotNull(response.getAccessToken());
    }

    // ── Test email déjà utilisé ───────────────
    @Test
    void registerCandidate_emailAlreadyExists() {
        CandidateRegisterRequest req =
            new CandidateRegisterRequest();
        req.setEmail("existing@test.com");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");
        req.setFirstName("Test");
        req.setLastName("User");

        when(userRepository.existsByEmail(
            "existing@test.com"))
            .thenReturn(true);

        assertThrows(
            EmailAlreadyExistsException.class,
            () -> authService
                .registerCandidate(req));
    }

    // ── Test mots de passe différents ─────────
    @Test
    void registerCandidate_passwordMismatch() {
        CandidateRegisterRequest req =
            new CandidateRegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("pass123");
        req.setConfirmPassword("different");
        req.setFirstName("Test");
        req.setLastName("User");

        when(userRepository.existsByEmail(
            anyString())).thenReturn(false);

        assertThrows(
            PasswordMismatchException.class,
            () -> authService
                .registerCandidate(req));
    }

    // ── Test inscription réussie ──────────────
    @Test
    void registerCandidate_success() {
        CandidateRegisterRequest req =
            new CandidateRegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");
        req.setFirstName("Ahmed");
        req.setLastName("Ben Ali");

        when(userRepository.existsByEmail(
            anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString()))
            .thenReturn("hashed");
        when(userRepository.save(any()))
            .thenReturn(mockUser);
        when(jwtUtil.generateToken(any()))
            .thenReturn("token");
        when(refreshTokenService
            .createRefreshToken(any()))
            .thenReturn(
                com.example.assistant_rh
                    .entity.RefreshToken.builder()
                    .token("rt")
                    .user(mockUser)
                    .expiresAt(
                        java.time.LocalDateTime
                        .now().plusDays(7))
                    .build());

        AuthResponse response =
            authService.registerCandidate(req);

        assertNotNull(response);
        verify(userRepository, times(1))
            .save(any());
    }
}
