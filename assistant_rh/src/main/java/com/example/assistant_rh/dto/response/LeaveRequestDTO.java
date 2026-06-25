package com.example.assistant_rh.dto.response;

import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.LeaveType;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequestDTO {
    private Long id;
    private Long employeeId;
    private String employeeFullName;
    private String employeeDepartment;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType type;
    private LeaveStatus status;
    private String reason;
    private String adminComment;
    private long numberOfDays;
    private LocalDateTime createdAt;
    private String approvedByEmail;
    private LocalDateTime approvedAt;
}