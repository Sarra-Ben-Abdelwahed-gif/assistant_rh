package com.example.assistant_rh.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message =
        "refresh token is required")
    private String refreshToken;
}
