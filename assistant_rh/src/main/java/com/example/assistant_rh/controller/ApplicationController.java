package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.response.ApplicationDTO;
import com.example.assistant_rh.enums.ApplicationStatus;
import com.example.assistant_rh.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Les nouveaux imports indispensables pour la pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/apply/{jobOfferId}")
    public ResponseEntity<ApplicationDTO> apply(
            @PathVariable Long jobOfferId,
            @RequestParam("candidateName") String name,
            @RequestParam("candidateEmail") String email,
            @RequestParam(value = "candidatePhone", required = false) String phone,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam(value = "cv", required = false) MultipartFile cv) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(
                        jobOfferId, name, email,
                        phone, coverLetter, cv));
    }

    // --- MÉTHODE MODIFIÉE POUR LA PAGINATION ---
    @GetMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<Page<ApplicationDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ApplicationDTO> result = applicationService.getAll(pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/job/{jobOfferId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<List<ApplicationDTO>> getByJobOffer(@PathVariable Long jobOfferId) {
        return ResponseEntity.ok(applicationService.getByJobOffer(jobOfferId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<ApplicationDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String hrComment) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status, hrComment));
    }
}
