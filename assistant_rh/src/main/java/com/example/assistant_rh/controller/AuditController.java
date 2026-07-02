package com.example.assistant_rh.controller;

import com.example.assistant_rh.entity.AuditLog;
import com.example.assistant_rh.repository
    .AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost
    .PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit",
    description = "Logs d'audit — Admin seulement")
public class AuditController {

    private final AuditLogRepository
        auditLogRepository;

    @Operation(summary = "Tous les logs d'audit")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<List<AuditLog>>
            getAll() {
        return ResponseEntity.ok(
            auditLogRepository.findAll());
    }

    @Operation(summary = "Logs par utilisateur")
    @GetMapping("/user/{email}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<List<AuditLog>>
            getByUser(
            @PathVariable String email) {
        return ResponseEntity.ok(
            auditLogRepository
                .findByUserEmail(email));
    }

    @Operation(summary = "Actions échouées")
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<List<AuditLog>>
            getFailed() {
        return ResponseEntity.ok(
            auditLogRepository
                .findBySuccessFalse());
    }
}
