package com.example.assistant_rh.dto.response;

import com.example.assistant_rh.enums.EmployeeStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmployeeDashboardDTO {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String position;
    private LocalDate hireDate;
    private EmployeeStatus status;

    
    private int annualLeaveBalance;
    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;

    
    private long totalDocuments;

    
    private List<LeaveRequestDTO> recentLeaves;
}
