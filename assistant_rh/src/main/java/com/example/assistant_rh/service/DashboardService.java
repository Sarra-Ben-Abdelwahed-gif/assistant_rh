package com.example.assistant_rh.service;

import com.example.assistant_rh.dto.response.DashboardDTO;
import com.example.assistant_rh.enums.ApplicationStatus;
import com.example.assistant_rh.enums.EmployeeStatus;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.LeaveType;
import com.example.assistant_rh.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRepository;
    private final DocumentRepository documentRepository;
    private final JobOfferRepository jobOfferRepository;
    private final ApplicationRepository applicationRepository;

    public DashboardDTO getDashboard() {
        DashboardDTO dto = new DashboardDTO();

        dto.setTotalEmployees(
            employeeRepository.count());
        dto.setActiveEmployees(
            employeeRepository.countByStatus(
                EmployeeStatus.ACTIVE));
        dto.setPendingLeaves(
            leaveRepository.countByStatus(
                LeaveStatus.PENDING));
        dto.setApprovedLeaves(
            leaveRepository.countByStatus(
                LeaveStatus.APPROVED));
        dto.setTotalDocuments(
            documentRepository.count());
        dto.setActiveJobOffers(
            jobOfferRepository.countByActiveTrue());
        dto.setTotalApplications(
            applicationRepository.count());
        dto.setPendingApplications(
            applicationRepository.countByStatus(
                ApplicationStatus.PENDING));

        
        Map<String, Long> byDept = new HashMap<>();
        employeeRepository.findAll().forEach(e -> {
            if (e.getDepartment() != null)
                byDept.merge(e.getDepartment(),
                    1L, Long::sum);
        });
        dto.setEmployeesByDepartment(byDept);

        
        Map<String, Long> byLeaveType = new HashMap<>();
        for (LeaveType type : LeaveType.values())
            byLeaveType.put(type.name(),
                leaveRepository.countByType(type));
        dto.setLeavesByType(byLeaveType);

        
        Map<String, Long> byAppStatus = new HashMap<>();
        for (ApplicationStatus status :
                ApplicationStatus.values())
            byAppStatus.put(status.name(),
                applicationRepository.countByStatus(status));
        dto.setApplicationsByStatus(byAppStatus);

        return dto;
    }
}