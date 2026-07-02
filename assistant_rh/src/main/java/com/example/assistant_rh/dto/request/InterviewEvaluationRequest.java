package com.example.assistant_rh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InterviewEvaluationRequest {

    @NotNull(message = "Application ID "
        + "is required")
    private Long applicationId;

    @NotBlank(message = "Interview notes "
        + "are required")
    private String interviewNotes;

    private String interviewDate;
    private String interviewerName;
}
