package com.example.assistant_rh.repository;

import com.example.assistant_rh.entity.AuditLog;
import org.springframework.data.jpa.repository
    .JpaRepository;
import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserEmail(String email);
    List<AuditLog> findByEntityTypeAndEntityId(
        String type, String id);
    List<AuditLog> findBySuccessFalse();
}
