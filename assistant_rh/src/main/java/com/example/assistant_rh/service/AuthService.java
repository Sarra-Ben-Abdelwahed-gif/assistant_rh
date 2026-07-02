package com.example.assistant_rh.service;

import com.example.assistant_rh.dto.request.*;
import com.example.assistant_rh.dto.response.AuthResponse;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.RefreshToken;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.enums.EmployeeStatus;
import com.example.assistant_rh.enums.Role;
import com.example.assistant_rh.exception.BadRequestException;
import com.example.assistant_rh.exception.EmailAlreadyExistsException;
import com.example.assistant_rh.exception.PasswordMismatchException;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.UserRepository;
import com.example.assistant_rh.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ─── Login ───────────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()));

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User", "email",
                        request.getEmail()));

        log.info("Login successful : {}",
            user.getEmail());

        return buildAuthResponse(user);
    }

    // ───  Admin/Employee registration ────────────────────
    public AuthResponse register(RegisterRequest request) {

        
        if (!request.getPassword()
                .equals(request.getConfirmPassword()))
            throw new PasswordMismatchException();

        if (userRepository.existsByEmail(
                request.getEmail()))
            throw new EmailAlreadyExistsException(
                request.getEmail());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(
                    request.getPassword()))
                .role(request.getRole())
                .build();
        userRepository.save(user);

        if (request.getRole() == Role.EMPLOYEE) {
            Employee employee = Employee.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .department(request.getDepartment())
                    .position(request.getPosition())
                    .hireDate(request.getHireDate() != null
                        ? request.getHireDate()
                        : LocalDate.now())
                    .birthDate(request.getBirthDate())
                    .status(EmployeeStatus.ACTIVE)
                    .user(user)
                    .build();
            employeeRepository.save(employee);
        }

        log.info("User created : {} ({})",
            user.getEmail(), user.getRole());

        return buildAuthResponse(user);
    }

    // ─── Candidate Registration (Public) ─────────────
    public AuthResponse registerCandidate(
            CandidateRegisterRequest request) {

        // Verify password match
        if (!request.getPassword()
                .equals(request.getConfirmPassword()))
            throw new PasswordMismatchException();

        if (userRepository.existsByEmail(
                request.getEmail()))
            throw new EmailAlreadyExistsException(
                request.getEmail());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(
                    request.getPassword()))
                .role(Role.CANDIDATE)
                .build();
        userRepository.save(user);

        log.info("Candidate registered : {}",
            user.getEmail());

        return buildAuthResponse(user);
    }

    // ─── Update Profile ─────────────────────────────
    public AuthResponse updateProfile(
            UpdateProfileRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User", "email", email));

        // Change password if requested
        if (request.getNewPassword() != null
                && !request.getNewPassword().isBlank()) {

            // verify old password
            if (request.getCurrentPassword() == null
                    || !passwordEncoder.matches(
                        request.getCurrentPassword(),
                        user.getPassword()))
                throw new BadRequestException(
                    "password mismatch: current password is incorrect");

            // Verify new password match
            if (!request.getNewPassword().equals(
                    request.getConfirmNewPassword()))
                throw new PasswordMismatchException();

            user.setPassword(passwordEncoder.encode(
                request.getNewPassword()));
        }

        userRepository.save(user);

        // update employee profile if necessary 
        if (user.getRole() == Role.EMPLOYEE) {
            employeeRepository
                .findByEmail(email)
                .ifPresent(emp -> {
                    if (request.getFirstName() != null
                            && !request.getFirstName()
                                .isBlank())
                        emp.setFirstName(
                            request.getFirstName());
                    if (request.getLastName() != null
                            && !request.getLastName()
                                .isBlank())
                        emp.setLastName(
                            request.getLastName());
                    if (request.getPhone() != null
                            && !request.getPhone()
                                .isBlank())
                        emp.setPhone(request.getPhone());
                    employeeRepository.save(emp);
                });
        }

        log.info("Profile updated : {}", email);
        return buildAuthResponse(user);
    }

    
    private AuthResponse buildAuthResponse(User user) {
    RefreshToken refresh =
        refreshTokenService
            .createRefreshToken(user);

    return AuthResponse.builder()
        .accessToken(jwtUtil.generateToken(user))
        .refreshToken(refresh.getToken())
        .tokenType("Bearer")
        .expiresIn(86400)
        .email(user.getEmail())
        .role(user.getRole())
        .userId(user.getId())
        .build();
    }
    public AuthResponse refreshToken(
        String refreshTokenStr) {

    RefreshToken refreshToken =
        refreshTokenService
            .validate(refreshTokenStr);

    User user = refreshToken.getUser();
    return buildAuthResponse(user);
    }
    public void logout(String refreshTokenStr) {
    RefreshToken refreshToken =
        refreshTokenService
            .validate(refreshTokenStr);
    refreshTokenService
        .revokeAll(refreshToken.getUser());
    log.info("Logout : {}",
        refreshToken.getUser().getEmail());
}
}