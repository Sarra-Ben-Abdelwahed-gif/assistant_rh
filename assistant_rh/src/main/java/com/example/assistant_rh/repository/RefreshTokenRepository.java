package com.example.assistant_rh.repository;

import com.example.assistant_rh.entity.RefreshToken;
import com.example.assistant_rh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(
        String token);

    @Modifying
    @Query("UPDATE RefreshToken r "
        + "SET r.revoked = true "
        + "WHERE r.user = :user")
    void revokeAllUserTokens(User user);

    void deleteByUser(User user);
}
