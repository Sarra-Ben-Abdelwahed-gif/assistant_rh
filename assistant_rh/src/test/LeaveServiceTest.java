package com.example.assistant_rh.service;

import com.example.assistant_rh.config
    .MapperConfig;
import com.example.assistant_rh.dto.request
    .LeaveRequestCreate;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.LeaveType;
import com.example.assistant_rh.exception
    .BadRequestException;
import com.example.assistant_rh.exception
    .InsufficientLeaveBalanceException;
import com.example.assistant_rh.repository
    .EmployeeRepository;
import com.example.assistant_rh.repository
    .LeaveRequestRepository;
import com.example.assistant_rh.repository
    .UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension
    .ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context
    .SecurityContext;
import org.springframework.security.core.context
    .SecurityContextHolder;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MapperConfig mapper;
    @InjectMocks
    private LeaveService leaveService;

    private void mockSecurityContext(
            String email) {
        Authentication auth =
            mock(Authentication.class);
        SecurityContext ctx =
            mock(SecurityContext.class);
        when(auth.getName()).thenReturn(email);
        when(ctx.getAuthentication())
            .thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ── Test solde insuffisant ────────────────
    @Test
    void create_insufficientBalance() {
        mockSecurityContext("emp@test.com");

        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmail("emp@test.com");
        emp.setAnnualLeaveBalance(5);

        when(employeeRepository
            .findByEmail("emp@test.com"))
            .thenReturn(Optional.of(emp));

        LeaveRequestCreate req =
            new LeaveRequestCreate();
        req.setStartDate(
            LocalDate.now().plusDays(1));
        req.setEndDate(
            LocalDate.now().plusDays(20));
        req.setType(LeaveType.ANNUAL);

        when(leaveRepository
            .existsOverlappingLeave(
                any(), any(), any()))
            .thenReturn(false);

        assertThrows(
            InsufficientLeaveBalanceException.class,
            () -> leaveService.create(req));
    }

    // ── Test chevauchement dates ──────────────
    @Test
    void create_overlappingDates() {
        mockSecurityContext("emp@test.com");

        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmail("emp@test.com");
        emp.setAnnualLeaveBalance(30);

        when(employeeRepository
            .findByEmail("emp@test.com"))
            .thenReturn(Optional.of(emp));

        when(leaveRepository
            .existsOverlappingLeave(
                any(), any(), any()))
            .thenReturn(true);

        LeaveRequestCreate req =
            new LeaveRequestCreate();
        req.setStartDate(
            LocalDate.now().plusDays(1));
        req.setEndDate(
            LocalDate.now().plusDays(5));
        req.setType(LeaveType.ANNUAL);

        assertThrows(
            BadRequestException.class,
            () -> leaveService.create(req));
    }

    // ── Test date fin avant début ─────────────
    @Test
    void create_endDateBeforeStart() {
        mockSecurityContext("emp@test.com");

        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmail("emp@test.com");
        emp.setAnnualLeaveBalance(30);

        when(employeeRepository
            .findByEmail("emp@test.com"))
            .thenReturn(Optional.of(emp));

        LeaveRequestCreate req =
            new LeaveRequestCreate();
        req.setStartDate(
            LocalDate.now().plusDays(10));
        req.setEndDate(
            LocalDate.now().plusDays(5));
        req.setType(LeaveType.ANNUAL);

        assertThrows(
            BadRequestException.class,
            () -> leaveService.create(req));
    }
}
