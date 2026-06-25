package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.response.DocumentDTO;
import com.example.assistant_rh.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<DocumentDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeId") Long employeeId,
            @RequestParam(value = "category",
                defaultValue = "AUTRE") String category,
            @RequestParam(value = "description",
                required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(
                    file, employeeId, category, description));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN_RH','EMPLOYEE')")
    public ResponseEntity<List<DocumentDTO>> getByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
            documentService.getByEmployee(employeeId));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN_RH','EMPLOYEE')")
    public ResponseEntity<DocumentDTO> getDownloadUrl(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            documentService.getDownloadUrl(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
