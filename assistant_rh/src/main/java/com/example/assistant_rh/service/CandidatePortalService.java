package com.example.assistant_rh.service;

import com.example.assistant_rh.config.MapperConfig;
import com.example.assistant_rh.dto.response.ApplicationDTO;
import com.example.assistant_rh.dto.response.CandidateDashboardDTO;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.enums.ApplicationStatus;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.exception.UnauthorizedAccessException;
import com.example.assistant_rh.repository.ApplicationRepository;
import com.example.assistant_rh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatePortalService {

    private final ApplicationRepository
        applicationRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final MapperConfig mapper;

    public CandidateDashboardDTO getMyDashboard() {
        String email = getCurrentEmail();
        getCurrentUser(email);

        var apps = applicationRepository
            .findByCandidateEmail(email);

        CandidateDashboardDTO dto =
            new CandidateDashboardDTO();
        dto.setEmail(email);
        dto.setTotalApplications(apps.size());
        dto.setPendingApplications(apps.stream()
            .filter(a ->
                a.getStatus() == ApplicationStatus.PENDING
                || a.getStatus() == ApplicationStatus.REVIEWED
                || a.getStatus() == ApplicationStatus.INTERVIEW)
            .count());
        dto.setAcceptedApplications(apps.stream()
            .filter(a ->
                a.getStatus() == ApplicationStatus.ACCEPTED)
            .count());
        dto.setRejectedApplications(apps.stream()
            .filter(a ->
                a.getStatus() == ApplicationStatus.REJECTED)
            .count());
        dto.setRecentApplications(apps.stream()
            .sorted((a, b) -> {
                if (a.getAppliedAt() == null) return 1;
                if (b.getAppliedAt() == null) return -1;
                return b.getAppliedAt()
                    .compareTo(a.getAppliedAt());
            })
            .limit(5)
            .map(this::enrichWithUrl)
            .collect(Collectors.toList()));

        return dto;
    }

    public List<ApplicationDTO> getMyCandidatures() {
        String email = getCurrentEmail();
        return applicationRepository
            .findByCandidateEmail(email)
            .stream()
            .map(this::enrichWithUrl)
            .collect(Collectors.toList());
    }

    public ApplicationDTO getMyCandidature(Long id) {
        String email = getCurrentEmail();
        var app = applicationRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Application", "id", id));

        if (!app.getCandidateEmail().equals(email))
            throw new UnauthorizedAccessException();

        return enrichWithUrl(app);
    }

    private ApplicationDTO enrichWithUrl(
            com.example.assistant_rh.entity
                .Application a) {
        ApplicationDTO dto = mapper.toApplicationDTO(a);
        if (a.getCvMinioKey() != null) {
            try {
                dto.setCvDownloadUrl(
                    minioService.generateDownloadUrl(
                        a.getCvMinioKey()));
            } catch (Exception ignored) {}
        }
        return dto;
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "User", "email", email));
    }

    private String getCurrentEmail() {
        return SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();
    }
}
