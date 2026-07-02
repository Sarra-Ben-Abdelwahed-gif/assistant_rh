package com.example.assistant_rh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy =
        GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private String action;
    private String entityType;
    private String entityId;

    @Column(length = 2000)
    private String details;

    private String ipAddress;
    private LocalDateTime timestamp;
    private boolean success;
}
