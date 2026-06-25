package com.example.assistant_rh.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class JobOfferDTO {
    private Long id;
    private String title;
    private String description;
    private String department;
    private String location;
    private String contractType;
    private String experienceRequired;
    private String salaryRange;
    private LocalDate deadline;
    private boolean active;
    private LocalDateTime createdAt;
    private long applicationCount;
}
