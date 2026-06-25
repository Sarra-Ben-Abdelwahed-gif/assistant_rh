package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.request.EmployeeRequest;
import com.example.assistant_rh.dto.response.EmployeeDTO;
import com.example.assistant_rh.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<List<EmployeeDTO>> getAll() {
        return ResponseEntity.ok(
            employeeService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<EmployeeDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            employeeService.getById(id));
    }

    @GetMapping("/departments")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(
            employeeService.getAllDepartments());
    }

    @GetMapping("/department/{dept}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<List<EmployeeDTO>> getByDepartment(
            @PathVariable String dept) {
        return ResponseEntity.ok(
            employeeService.getByDepartment(dept));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<EmployeeDTO> create(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<EmployeeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(
            employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_RH')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
