package com.example.assistant_rh.dto.response;

import com.example.assistant_rh.enums.ApplicationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationDTO {
    private Long id;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String cvFileName;
    private String coverLetter;
    private ApplicationStatus status;
    private Integer score;
    private String aiAnalysis;
    private String hrComment;
    private LocalDateTime appliedAt;
    private Long jobOfferId;
    private String jobOfferTitle;
    private String cvDownloadUrl;
}