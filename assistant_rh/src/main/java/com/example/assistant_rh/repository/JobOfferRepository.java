package com.example.assistant_rh.repository;

import com.example.assistant_rh.entity.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface JobOfferRepository
        extends JpaRepository<JobOffer, Long> {
    List<JobOffer> findByActiveTrue();
    List<JobOffer> findByDepartment(String department);
    long countByActiveTrue();
    List<JobOffer> findByActiveTrueAndDeadlineAfter(LocalDate date);
}