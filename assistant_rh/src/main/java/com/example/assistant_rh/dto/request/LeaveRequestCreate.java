package com.example.assistant_rh.dto.request;

import com.example.assistant_rh.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestCreate {

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;

    @NotNull(message = "Le type de congé est obligatoire")
    private LeaveType type;

    private String reason;
}