package com.example.assistant_rh.service;

import com.example.assistant_rh.config.MapperConfig;
import com.example.assistant_rh.dto.request.EmployeeRequest;
import com.example.assistant_rh.dto.response.EmployeeDTO;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.enums.EmployeeStatus;
import com.example.assistant_rh.enums.Role;
import com.example.assistant_rh.exception.EmailAlreadyExistsException;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MapperConfig mapper;

    
    public Page<EmployeeDTO> getAll(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(mapper::toEmployeeDTO);
    }

    public EmployeeDTO getById(Long id) {
        return mapper.toEmployeeDTO(
            employeeRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "id", id)));
    }

    public EmployeeDTO getByEmail(String email) {
        return mapper.toEmployeeDTO(
            employeeRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "email", email)));
    }

    public List<EmployeeDTO> getByDepartment(
            String department) {
        return employeeRepository
                .findByDepartment(department)
                .stream()
                .map(mapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllDepartments() {
        return employeeRepository.findAllDepartments();
    }

    public EmployeeDTO create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(
                request.getEmail()))
            throw new EmailAlreadyExistsException(
                request.getEmail());

        String rawPassword = request.getPassword() != null
                && !request.getPassword().isBlank()
                ? request.getPassword()
                : "Password@123";

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.EMPLOYEE)
                .build();
        userRepository.save(user);

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .position(request.getPosition())
                .address(request.getAddress())
                .hireDate(request.getHireDate() != null
                    ? request.getHireDate()
                    : LocalDate.now())
                .birthDate(request.getBirthDate())
                .status(request.getStatus() != null
                    ? request.getStatus()
                    : EmployeeStatus.ACTIVE)
                .annualLeaveBalance(
                    request.getAnnualLeaveBalance() > 0
                    ? request.getAnnualLeaveBalance() : 30)
                .user(user)
                .build();
        employeeRepository.save(employee);

        log.info("Employee created : {}", employee.getEmail());
        return mapper.toEmployeeDTO(employee);
    }

    public EmployeeDTO update(Long id,
            EmployeeRequest request) {
        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "id", id));

        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(
                    request.getEmail()))
            throw new EmailAlreadyExistsException(
                request.getEmail());

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setAddress(request.getAddress());
        employee.setHireDate(request.getHireDate());
        employee.setBirthDate(request.getBirthDate());
        if (request.getStatus() != null)
            employee.setStatus(request.getStatus());
        if (request.getAnnualLeaveBalance() > 0)
            employee.setAnnualLeaveBalance(
                request.getAnnualLeaveBalance());

        log.info("Employee updated : id={}", id);
        return mapper.toEmployeeDTO(
            employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "id", id));
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
        log.info("Employee deactivated : id={}", id);
    }
}