package com.example.assistant_rh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CvAnalysisResponse {
    private Integer score;
    private String summary;
    private String strengths;
    private String weaknesses;
    private String recommendation;
    private String fullAnalysis;
}