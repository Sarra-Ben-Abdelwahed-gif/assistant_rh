package com.example.assistant_rh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {

    @NotBlank(message =
        "Message cannot be empty")
    @Size(min = 1, max = 2000,
        message = "Message must be between "
            + "1 and 2000 characters")
    private String message;

    private List<Map<String, String>> history;
}
