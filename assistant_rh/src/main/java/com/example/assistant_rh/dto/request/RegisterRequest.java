package com.example.assistant_rh.dto.request;

import com.example.assistant_rh.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Minimum 6 caractères")
    private String password;

    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmPassword;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    private String department;
    private String position;
    private String phone;
    private LocalDate hireDate;
    private LocalDate birthDate;
}