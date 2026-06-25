package com.example.assistant_rh.dto.response;

import com.example.assistant_rh.enums.EmployeeStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String position;
    private String address;
    private LocalDate hireDate;
    private LocalDate birthDate;
    private EmployeeStatus status;
    private int annualLeaveBalance;
    private LocalDateTime createdAt;
}
