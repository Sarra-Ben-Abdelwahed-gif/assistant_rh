package com.example.assistant_rh.service;

import com.example.assistant_rh.dto.response.AuthResponse;
import com.example.assistant_rh.entity.AuditLog;
import com.example.assistant_rh.entity.RefreshToken;
import com.example.assistant_rh.repository
    .AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation
    .Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository
        auditLogRepository;

    @Async
    public void log(
            String userEmail,
            String action,
            String entityType,
            String entityId,
            String details,
            boolean success) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .userEmail(userEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .success(success)
                .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Audit log error : {}",
                e.getMessage());
        }
    }

    // Actions prédéfinies
    public void logLogin(String email,
            boolean success) {
        log(email, "LOGIN", "USER",
            email,
            success ? "Login successful"
                : "Failed login attempt",
            success);
    }

    public void logLeaveCreated(
            String email, Long leaveId) {
        log(email, "CREATE_LEAVE",
            "LEAVE_REQUEST",
            String.valueOf(leaveId),
            "Leave request created",
            true);
    }

    public void logLeaveApproved(
            String adminEmail, Long leaveId,
            String status) {
        log(adminEmail, "UPDATE_LEAVE_STATUS",
            "LEAVE_REQUEST",
            String.valueOf(leaveId),
            "Status changed : " + status,
            true);
    }

    public void logDocumentUploaded(
            String email, Long docId,
            String fileName) {
        log(email, "UPLOAD_DOCUMENT",
            "DOCUMENT",
            String.valueOf(docId),
            "File uploaded : " + fileName,
            true);
    }

    public void logEmployeeCreated(
            String adminEmail, Long empId) {
        log(adminEmail, "CREATE_EMPLOYEE",
            "EMPLOYEE",
            String.valueOf(empId),
            "Employee created",
            true);
    }

    public void logMcpAction(
            String email, String action,
            String result) {
        log(email, "MCP_ACTION",
            "AI_ASSISTANT",
            action, result, true);
    }
}
