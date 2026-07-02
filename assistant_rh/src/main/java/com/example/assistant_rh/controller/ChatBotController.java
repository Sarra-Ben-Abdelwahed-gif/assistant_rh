package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.request.ChatRequest;
import com.example.assistant_rh.dto.response.ChatResponse;
import com.example.assistant_rh.service.ChatBotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    // Question simple sans historique
    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> ask(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(
            chatBotService.respondSimple(
                request.getMessage()));
    }

    // Conversation avec historique + MCP
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(
            chatBotService.respond(
                request.getMessage(),
                request.getHistory()));
    }
}
