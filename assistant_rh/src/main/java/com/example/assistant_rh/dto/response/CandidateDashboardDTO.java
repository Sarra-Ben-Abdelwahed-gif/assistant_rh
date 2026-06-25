package com.example.assistant_rh.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CandidateDashboardDTO {
    private String firstName;
    private String lastName;
    private String email;
    private long totalApplications;
    private long pendingApplications;
    private long acceptedApplications;
    private long rejectedApplications;
    private List<ApplicationDTO> recentApplications;
}
