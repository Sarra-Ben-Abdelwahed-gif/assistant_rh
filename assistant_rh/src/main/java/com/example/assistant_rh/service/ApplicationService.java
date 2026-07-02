package com.example.assistant_rh.service;

import com.example.assistant_rh.config.MapperConfig;
import com.example.assistant_rh.dto.response.ApplicationDTO;
import com.example.assistant_rh.entity.Application;
import com.example.assistant_rh.entity.JobOffer;
import com.example.assistant_rh.enums.ApplicationStatus;
import com.example.assistant_rh.exception.BadRequestException;
import com.example.assistant_rh.exception.FileUploadException;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.ApplicationRepository;
import com.example.assistant_rh.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobOfferRepository jobOfferRepository;
    private final MinioService minioService;
    private final MapperConfig mapper;

    public ApplicationDTO apply(Long jobOfferId,
            String candidateName, String candidateEmail,
            String candidatePhone, String coverLetter,
            MultipartFile cv) {

        JobOffer offer = jobOfferRepository
                .findById(jobOfferId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "JobOffer", "id", jobOfferId));

        if (!offer.isActive())
            throw new BadRequestException(
                "Cette offre n'est plus active");

        if (applicationRepository
                .existsByCandidateEmailAndJobOfferId(
                    candidateEmail, jobOfferId))
            throw new BadRequestException(
                "Vous avez déjà postulé à cette offre");

        String cvKey = null;
        String cvFileName = null;

        if (cv != null && !cv.isEmpty()) {
            try {
                cvKey = minioService.uploadFile(cv);
                cvFileName = cv.getOriginalFilename();
            } catch (Exception e) {
                throw new FileUploadException(e.getMessage());
            }
        }

        Application app = Application.builder()
                .candidateName(candidateName)
                .candidateEmail(candidateEmail)
                .candidatePhone(candidatePhone)
                .coverLetter(coverLetter)
                .cvMinioKey(cvKey)
                .cvFileName(cvFileName)
                .jobOffer(offer)
                .build();

        log.info("Candidature reçue : {} pour offre id={}",
            candidateEmail, jobOfferId);
        return mapper.toApplicationDTO(
            applicationRepository.save(app));
    }

    public Page<ApplicationDTO> getAll(Pageable pageable) {
        return applicationRepository.findAll(pageable)
                .map(this::enrichWithDownloadUrl);
    }

    public List<ApplicationDTO> getByJobOffer(
            Long jobOfferId) {
        if (!jobOfferRepository.existsById(jobOfferId))
            throw new ResourceNotFoundException(
                "JobOffer", "id", jobOfferId);

        return applicationRepository
                .findByJobOfferId(jobOfferId)
                .stream()
                .map(this::enrichWithDownloadUrl)
                .collect(Collectors.toList());
    }

    public ApplicationDTO updateStatus(Long id,
            ApplicationStatus status, String hrComment) {
        Application app = applicationRepository
                .findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Application", "id", id));

        app.setStatus(status);
        if (hrComment != null && !hrComment.isBlank())
            app.setHrComment(hrComment);

        log.info("Candidature {} : id={}",
            status, id);
        return mapper.toApplicationDTO(
            applicationRepository.save(app));
    }

    private ApplicationDTO enrichWithDownloadUrl(
            Application a) {
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
}
