package com.example.assistant_rh.repository;

import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Employee> findByDepartment(String department);
    List<Employee> findByStatus(EmployeeStatus status);
    long countByStatus(EmployeeStatus status);

    @Query("SELECT DISTINCT e.department FROM Employee e " +
           "WHERE e.department IS NOT NULL")
    List<String> findAllDepartments();
}