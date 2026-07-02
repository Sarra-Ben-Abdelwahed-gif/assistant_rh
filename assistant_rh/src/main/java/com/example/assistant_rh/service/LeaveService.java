package com.example.assistant_rh.service;

import com.example.assistant_rh.config.MapperConfig;
import com.example.assistant_rh.dto.request.LeaveRequestCreate;
import com.example.assistant_rh.dto.request.LeaveStatusUpdate;
import com.example.assistant_rh.dto.response.LeaveRequestDTO;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.LeaveType;
import com.example.assistant_rh.exception.BadRequestException;
import com.example.assistant_rh.exception.InsufficientLeaveBalanceException;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import com.example.assistant_rh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final MapperConfig mapper;

    public LeaveRequestDTO create(LeaveRequestCreate request) {
        String email = getCurrentEmail();
        Employee employee = employeeRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("The end date must be after the start date");
        }

        if (leaveRepository.existsOverlappingLeave(
                employee.getId(),
                request.getStartDate(),
                request.getEndDate())) {
            throw new BadRequestException("You already have a request for this period");
        }

        long days = java.time.temporal.ChronoUnit.DAYS
                .between(request.getStartDate(), request.getEndDate()) + 1;

        if (request.getType() == LeaveType.ANNUAL) {
            if (employee.getAnnualLeaveBalance() < days) {
                throw new InsufficientLeaveBalanceException(
                        employee.getAnnualLeaveBalance(), days);
            }
        }

        LeaveRequest leave = LeaveRequest.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .reason(request.getReason())
                .build();

        log.info("Leave request created: employee={}", employee.getEmail());
        return mapper.toLeaveDTO(
                leaveRepository.save(leave));
    }

    public Page<LeaveRequestDTO> getAll(Pageable pageable) {
        return leaveRepository.findAll(pageable)
                .map(mapper::toLeaveDTO);
    }

    public List<LeaveRequestDTO> getMyLeaves() {
        String email = getCurrentEmail();
        Employee employee = employeeRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));

        return leaveRepository
                .findByEmployeeId(employee.getId())
                .stream()
                .map(mapper::toLeaveDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDTO> getPending() {
        return leaveRepository
                .findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(mapper::toLeaveDTO)
                .collect(Collectors.toList());
    }

    public LeaveRequestDTO updateStatus(Long id, LeaveStatusUpdate update) {
        LeaveRequest leave = leaveRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("This request has already been processed");
        }

        String adminEmail = getCurrentEmail();
        User admin = userRepository
                .findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", adminEmail));

        leave.setStatus(update.getStatus());
        leave.setAdminComment(update.getAdminComment());
        leave.setApprovedBy(admin);
        leave.setApprovedAt(LocalDateTime.now());

        if (update.getStatus() == LeaveStatus.APPROVED) {
            Employee employee = leave.getEmployee();
            if (leave.getType() == LeaveType.ANNUAL) {
                long days = java.time.temporal.ChronoUnit.DAYS
                        .between(leave.getStartDate(), leave.getEndDate()) + 1;
                int newBalance = employee.getAnnualLeaveBalance() - (int) days;
                employee.setAnnualLeaveBalance(Math.max(newBalance, 0));
                employeeRepository.save(employee);
            }
        }

        log.info("Leave {} : id={} par {}", leave.getStatus(), id, adminEmail);
        return mapper.toLeaveDTO(
                leaveRepository.save(leave));
    }

    public void delete(Long id) {
        LeaveRequest leave = leaveRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Cannot delete a processed request");
        }
        leaveRepository.delete(leave);
        log.info("Leave request deleted : id={}", id);
    }

    private String getCurrentEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}
