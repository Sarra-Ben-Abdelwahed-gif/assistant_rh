package com.example.assistant_rh.dto.response;

import com.example.assistant_rh.service.mcp
    .McpProactiveAlertService.AlertType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class McpAlertDTO {
    private AlertType type;
    private String message;
    private String code;
    private LocalDate date;
    private String suggestedAction;
}
