package com.example.assistant_rh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String message;
    private String role;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorType;

    // Constructeur simple pour les réponses OK
    public ChatResponse(String message,
            String role) {
        this.message = message;
        this.role = role;
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    // Constructeur pour les erreurs
    public ChatResponse(String message,
            String role, boolean success,
            String errorType) {
        this.message = message;
        this.role = role;
        this.timestamp = LocalDateTime.now();
        this.success = success;
        this.errorType = errorType;
    }
}