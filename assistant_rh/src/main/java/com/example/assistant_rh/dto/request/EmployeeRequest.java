package com.example.assistant_rh.dto.request;

import com.example.assistant_rh.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Position is required")
    private String position;

    private String address;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    private LocalDate birthDate;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    private int annualLeaveBalance = 30;
}
