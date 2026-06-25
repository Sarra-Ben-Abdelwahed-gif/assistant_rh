package com.example.assistant_rh.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {

    @NotBlank(message = "Le message ne peut pas être vide")
    private String message;

    private List<Map<String, String>> history;
}
