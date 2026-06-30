package com.example.assistant_rh.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private List<Map<String, String>> history;
}
