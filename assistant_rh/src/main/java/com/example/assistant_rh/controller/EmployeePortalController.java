package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.response.DocumentDTO;
import com.example.assistant_rh.dto.response.EmployeeDashboardDTO;
import com.example.assistant_rh.service.EmployeePortalService;
import com.example.assistant_rh.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeePortalController {

    private final EmployeePortalService
        employeePortalService;
    private final PdfService pdfService;

    // Tableau de bord de l'employé connecté
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeDashboardDTO>
            getMyDashboard() {
        return ResponseEntity.ok(
            employeePortalService.getMyDashboard());
    }

    // Mes documents
    @GetMapping("/documents")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<DocumentDTO>>
            getMyDocuments() {
        return ResponseEntity.ok(
            employeePortalService.getMyDocuments());
    }

    // Télécharger un de mes documents
    @GetMapping("/documents/{id}/download")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<DocumentDTO>
            downloadMyDocument(@PathVariable Long id) {
        return ResponseEntity.ok(
            employeePortalService
                .getMyDocumentDownload(id));
    }

    // Générer attestation de travail (PDF)
    @GetMapping("/attestation")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<byte[]> getAttestation() {
        // Récupérer l'id de l'employé connecté
        EmployeeDashboardDTO dash =
            employeePortalService.getMyDashboard();
        byte[] pdf = pdfService
            .generateWorkCertificate(dash.getId());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=attestation.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    // Générer PDF d'un congé
    @GetMapping("/leaves/{leaveId}/pdf")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<byte[]> getLeavePdf(
            @PathVariable Long leaveId) {
        byte[] pdf = pdfService
            .generateLeavePdf(leaveId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=conge_"
                + leaveId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
