package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.response.CvAnalysisResponse;
import com.example.assistant_rh.service.CvAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cv-analysis")
@RequiredArgsConstructor
public class CvAnalysisController {

    private final CvAnalysisService cvAnalysisService;

    @PostMapping("/application/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<CvAnalysisResponse> analyze(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            cvAnalysisService.analyzeApplication(id));
    }

    @PostMapping("/quick/{jobOfferId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<CvAnalysisResponse> quickAnalyze(
            @PathVariable Long jobOfferId,
            @RequestBody Map<String, String> body) {
        String cvText = body.get("cvText");
        if (cvText == null || cvText.isBlank())
            throw new com.example.assistant_rh
                .exception.BadRequestException(
                "Le texte du CV est requis");
        return ResponseEntity.ok(
            cvAnalysisService.quickAnalyze(
                cvText, jobOfferId));
    }
}
