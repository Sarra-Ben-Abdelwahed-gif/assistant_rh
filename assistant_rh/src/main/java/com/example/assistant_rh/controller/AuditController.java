package com.example.assistant_rh.controller;

import com.example.assistant_rh.entity.AuditLog;
import com.example.assistant_rh.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit Logs - Admin only ")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @Operation(summary = "All audit logs")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_RH')") 
    public ResponseEntity<Page<AuditLog>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) { 

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLog> result = auditLogRepository.findAll(pageable);
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Logs by user")
    @GetMapping("/user/{email}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<Page<AuditLog>> getByUser(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLog> result = auditLogRepository.findByUserEmail(email, pageable);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Failed operations")
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<Page<AuditLog>> getFailed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLog> result = auditLogRepository.findBySuccessFalse(pageable);

        return ResponseEntity.ok(result);
    }
}
