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

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    private String phone;

    @NotBlank(message = "Le département est obligatoire")
    private String department;

    @NotBlank(message = "Le poste est obligatoire")
    private String position;

    private String address;

    @NotNull(message = "La date d'embauche est obligatoire")
    private LocalDate hireDate;

    private LocalDate birthDate;

    @Size(min = 6, message = "Minimum 6 caractères")
    private String password;

    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    private int annualLeaveBalance = 30;
}
