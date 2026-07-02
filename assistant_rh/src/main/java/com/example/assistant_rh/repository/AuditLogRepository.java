package com.example.assistant_rh.repository;

import com.example.assistant_rh.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByAction(String action);

    
    Page<AuditLog> findByUserEmail(String email, Pageable pageable);

    
    Page<AuditLog> findBySuccessFalse(Pageable pageable);
}
