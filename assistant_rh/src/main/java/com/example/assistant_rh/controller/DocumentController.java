package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.response.DocumentDTO;
import com.example.assistant_rh.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("type") String type,
            @RequestParam(value = "description", required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(file, employeeId, type, description));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<Page<DocumentDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DocumentDTO> result = documentService.getAll(pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<DocumentDTO>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(documentService.getByEmployee(employeeId));
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasRole('HR_ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<Void> download(@PathVariable Long id) {
        DocumentDTO dto = documentService.downloadDocument(id);
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, dto.getDownloadUrl())
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}