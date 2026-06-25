package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.response.ApplicationDTO;
import com.example.assistant_rh.dto.response.CandidateDashboardDTO;
import com.example.assistant_rh.service.CandidatePortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidatePortalController {

    private final CandidatePortalService
        candidatePortalService;

    // Tableau de bord du candidat
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateDashboardDTO>
            getMyDashboard() {
        return ResponseEntity.ok(
            candidatePortalService.getMyDashboard());
    }

    // Mes candidatures
    @GetMapping("/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ApplicationDTO>>
            getMyCandidatures() {
        return ResponseEntity.ok(
            candidatePortalService
                .getMyCandidatures());
    }

    // Détail d'une candidature
    @GetMapping("/applications/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationDTO>
            getMyCandidature(@PathVariable Long id) {
        return ResponseEntity.ok(
            candidatePortalService
                .getMyCandidature(id));
    }
}
