package com.example.assistant_rh.dto.request;

import com.example.assistant_rh.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveStatusUpdate {

    @NotNull(message = "Status is required")
    private LeaveStatus status;

    private String adminComment;
}