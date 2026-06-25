package com.example.assistant_rh.repository;

import com.example.assistant_rh.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {
    List<Document> findByEmployeeId(Long employeeId);
    List<Document> findByDocumentCategory(String category);
    long countByEmployeeId(Long employeeId);
    long count();
}