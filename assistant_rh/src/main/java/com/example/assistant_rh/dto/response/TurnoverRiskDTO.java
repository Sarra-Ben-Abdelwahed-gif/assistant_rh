package com.example.assistant_rh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurnoverRiskDTO {
    private String aiReport;
    private List<DepartmentRiskDTO> departments;
    private String generatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepartmentRiskDTO {
        private String department;
        private int totalEmployees;
        private int highRiskCount;
        private int burnoutRiskCount;
        private double riskScore;
        private String riskLevel;
    }
}
