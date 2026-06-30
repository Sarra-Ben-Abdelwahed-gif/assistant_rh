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

    @GetMapping("/Certificate/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<byte[]> Certificate(
            @PathVariable Long employeeId) {
        byte[] pdf = pdfService
            .generateWorkCertificate(employeeId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=Certificate_"
                + employeeId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/leave/{leaveId}")
    @PreAuthorize("hasAnyRole('HR_ADMIN','EMPLOYEE')")
    public ResponseEntity<byte[]> leavePdf(
            @PathVariable Long leaveId) {
        byte[] pdf = pdfService
            .generateLeavePdf(leaveId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=Leave_"
                + leaveId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<byte[]> employeeList() {
        byte[] pdf = pdfService
            .generateEmployeeListPdf();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=employees.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
