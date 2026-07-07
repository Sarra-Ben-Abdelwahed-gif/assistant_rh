package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.request
    .ChatRequest;
import com.example.assistant_rh.dto.response
    .ChatResponse;
import com.example.assistant_rh.service
    .ChatBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost
    .PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chatbot AI",
    description = "AI assistant with MCP")
public class ChatBotController {

    private final ChatBotService chatBotService;

    @Operation(
        summary = "Chat with history",
        description = "Full conversation "
            + "with context + MCP actions")
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody
            ChatRequest request) {
        try {
            // Vérifier message non vide
            if (request.getMessage() == null
                    || request.getMessage()
                        .isBlank()) {
                return ResponseEntity.ok(
                    new ChatResponse(
                        "Please type a message "
                        + "before sending.",
                        "assistant",
                        false,
                        "EMPTY_MESSAGE"));
            }

            ChatResponse response =
                chatBotService.respond(
                    request.getMessage(),
                    request.getHistory());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Chatbot error : {}",
                e.getMessage());
            return ResponseEntity.ok(
                new ChatResponse(
                    "I am temporarily "
                    + "unavailable. "
                    + "Please try again.",
                    "assistant",
                    false,
                    "SERVER_ERROR"));
        }
    }

    @Operation(
        summary = "Simple question",
        description = "Single question "
            + "without history")
    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> ask(
            @Valid @RequestBody
            ChatRequest request) {
        try {
            if (request.getMessage() == null
                    || request.getMessage()
                        .isBlank()) {
                return ResponseEntity.ok(
                    new ChatResponse(
                        "Please type a message "
                        + "before sending.",
                        "assistant",
                        false,
                        "EMPTY_MESSAGE"));
            }

            return ResponseEntity.ok(
                chatBotService.respondSimple(
                    request.getMessage()));

        } catch (Exception e) {
            log.error("Ask error : {}",
                e.getMessage());
            return ResponseEntity.ok(
                new ChatResponse(
                    "I am temporarily "
                    + "unavailable. "
                    + "Please try again.",
                    "assistant",
                    false,
                    "SERVER_ERROR"));
        }
    }
}
