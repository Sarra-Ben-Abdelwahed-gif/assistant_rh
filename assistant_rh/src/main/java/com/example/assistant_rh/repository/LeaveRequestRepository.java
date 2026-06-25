package com.example.assistant_rh.repository;

import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByType(LeaveType type);
    long countByStatus(LeaveStatus status);
    long countByType(LeaveType type);

    @Query("""
        SELECT COUNT(l) > 0 FROM LeaveRequest l
        WHERE l.employee.id = :employeeId
        AND l.status != 'REJECTED'
        AND l.startDate <= :endDate
        AND l.endDate >= :startDate
        """)
    boolean existsOverlappingLeave(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}