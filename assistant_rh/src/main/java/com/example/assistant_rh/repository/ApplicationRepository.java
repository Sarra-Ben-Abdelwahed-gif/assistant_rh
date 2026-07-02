package com.example.assistant_rh.repository;

import com.example.assistant_rh.entity.Application;
import com.example.assistant_rh.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByJobOfferId(Long jobOfferId);
    
    List<Application> findByStatus(ApplicationStatus status);
    
    List<Application> findByCandidateEmail(String email);
    
    boolean existsByCandidateEmailAndJobOfferId(String email, Long jobOfferId);
    
    long countByStatus(ApplicationStatus status);
    
    long countByJobOfferId(Long jobOfferId);
    
    List<Application> findByUserId(Long userId);

    
    
    
    Page<Application> findByJobOfferId(Long jobOfferId, Pageable pageable);
    
    
    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);
}