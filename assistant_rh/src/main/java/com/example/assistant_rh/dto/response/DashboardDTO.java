package com.example.assistant_rh.dto.response;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardDTO {
    private long totalEmployees;
    private long activeEmployees;
    private long pendingLeaves;
    private long approvedLeaves;
    private long totalDocuments;
    private long activeJobOffers;
    private long totalApplications;
    private long pendingApplications;
    private Map<String, Long> employeesByDepartment;
    private Map<String, Long> leavesByType;
    private Map<String, Long> applicationsByStatus;
}