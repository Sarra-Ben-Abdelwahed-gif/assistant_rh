package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.request.LeaveRequestCreate;
import com.example.assistant_rh.dto.request.LeaveStatusUpdate;
import com.example.assistant_rh.dto.response.LeaveRequestDTO;
import com.example.assistant_rh.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveRequestDTO> create(
            @Valid @RequestBody LeaveRequestCreate request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.create(request));
    }

    
    @GetMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<Page<LeaveRequestDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestDTO> result = leaveService.getAll(pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<LeaveRequestDTO>> getMyLeaves() {
        return ResponseEntity.ok(leaveService.getMyLeaves());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<List<LeaveRequestDTO>> getPending() {
        return ResponseEntity.ok(leaveService.getPending());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<LeaveRequestDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody LeaveStatusUpdate update) {
        return ResponseEntity.ok(leaveService.updateStatus(id, update));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
