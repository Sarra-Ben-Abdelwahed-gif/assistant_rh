package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.response
    .DocumentDTO;
import com.example.assistant_rh.dto.response
    .DocumentUploadResponse;
import com.example.assistant_rh.service
    .DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost
    .PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart
    .MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents",
    description = "Document management")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
        summary = "Upload a document",
        description = "Returns success/error "
            + "with file details")
    @PostMapping(
        value = "/upload",
        consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<DocumentUploadResponse>
            upload(
            @RequestParam("file")
                MultipartFile file,
            @RequestParam("employeeId")
                Long employeeId,
            @RequestParam("category")
                String category,
            @RequestParam(value = "description",
                required = false,
                defaultValue = "")
                String description) {

        DocumentUploadResponse response =
            documentService.upload(
                file, employeeId,
                category, description);

        // Return 200 OK even on error to allow the frontend to parse the message
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get documents by employee")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<List<DocumentDTO>>
            getByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
            documentService
                .getByEmployee(employeeId));
    }

    @Operation(
        summary = "Get download URL")
    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDTO>
            getDownloadUrl(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            documentService.getDownloadUrl(id));
    }

    @Operation(summary = "Delete a document")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}