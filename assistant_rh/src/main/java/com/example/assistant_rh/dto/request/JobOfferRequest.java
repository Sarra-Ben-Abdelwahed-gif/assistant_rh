package com.example.assistant_rh.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class JobOfferRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotBlank(message = "Le département est obligatoire")
    private String department;

    private String location;
    private String contractType;
    private String experienceRequired;
    private String salaryRange;

    @NotNull(message = "La date limite est obligatoire")
    @Future(message = "La date limite doit être dans le futur")
    private LocalDate deadline;
}