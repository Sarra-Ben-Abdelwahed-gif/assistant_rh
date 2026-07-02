package com.example.assistant_rh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConflictAnalysisDTO {
    private boolean hasConflict;
    private int absenceRatePercent;
    private List<ConflictInfoDTO> conflicts;
    private String alternativeDates;
    private String recommendation;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConflictInfoDTO {
        private String employeeName;
        private String position;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
    }
}
