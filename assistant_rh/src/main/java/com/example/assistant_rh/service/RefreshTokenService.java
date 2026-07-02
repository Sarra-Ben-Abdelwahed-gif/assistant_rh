package com.example.assistant_rh.service;

import com.example.assistant_rh.entity.RefreshToken;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.exception.BadRequestException;
import com.example.assistant_rh.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    public RefreshToken createRefreshToken(User user) {
        // Revoke any existing active tokens for the user
        refreshTokenRepository.revokeAllUserTokens(user);

        RefreshToken token = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS))
            .revoked(false)
            .build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new BadRequestException("Invalid refresh token."));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token has been revoked.");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token has expired. Please log in again.");
        }

        return refreshToken;
    }

    public void revokeAll(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }
}
