package com.example.assistant_rh.controller;

import com.example.assistant_rh.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @GetMapping("/attestation/{employeeId}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<byte[]> attestation(
            @PathVariable Long employeeId) {
        byte[] pdf = pdfService
            .generateWorkCertificate(employeeId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=attestation_"
                + employeeId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/leave/{leaveId}")
    @PreAuthorize("hasAnyRole('ADMIN_RH','EMPLOYEE')")
    public ResponseEntity<byte[]> leavePdf(
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

    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<byte[]> employeeList() {
        byte[] pdf = pdfService
            .generateEmployeeListPdf();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=employes.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
