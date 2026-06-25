package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.request.ChatRequest;
import com.example.assistant_rh.dto.response.ChatResponse;
import com.example.assistant_rh.service.ChatBotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> ask(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(
            chatBotService.respondSimple(
                request.getMessage()));
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        List<Map<String, String>> history =
            (List<Map<String, String>>)
            body.getOrDefault("history", List.of());
        return ResponseEntity.ok(
            chatBotService.respond(message, history));
    }
}
