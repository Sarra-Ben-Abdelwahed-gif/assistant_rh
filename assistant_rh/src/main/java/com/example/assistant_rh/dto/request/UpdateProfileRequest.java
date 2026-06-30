package com.example.assistant_rh.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String phone;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    private String confirmNewPassword;

    private String currentPassword;
}