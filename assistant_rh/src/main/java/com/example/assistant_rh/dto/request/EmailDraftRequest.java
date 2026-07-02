package com.example.assistant_rh.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailDraftRequest {

    @NotNull
    private Long applicationId;

    private String feedback;
    private String interviewDateTime;
    private String interviewLocation;

    public enum EmailType {
        HIRE, REJECTION, INTERVIEW_INVITE
    }

    @NotNull
    private EmailType emailType;
}
