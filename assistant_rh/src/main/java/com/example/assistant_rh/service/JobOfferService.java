package com.example.assistant_rh.service;

import com.example.assistant_rh.config.MapperConfig;
import com.example.assistant_rh.dto.request.JobOfferRequest;
import com.example.assistant_rh.dto.response.JobOfferDTO;
import com.example.assistant_rh.entity.JobOffer;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.JobOfferRepository;
import com.example.assistant_rh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;
    private final MapperConfig mapper;

    public JobOfferDTO create(JobOfferRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User admin = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User", "email", email));

        JobOffer offer = JobOffer.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .department(request.getDepartment())
                .location(request.getLocation())
                .contractType(request.getContractType())
                .experienceRequired(
                    request.getExperienceRequired())
                .salaryRange(request.getSalaryRange())
                .deadline(request.getDeadline())
                .createdBy(admin)
                .build();

        log.info("Offre créée : {}", offer.getTitle());
        return mapper.toJobOfferDTO(
            jobOfferRepository.save(offer));
    }

    public List<JobOfferDTO> getAll() {
        return jobOfferRepository.findAll()
                .stream()
                .map(mapper::toJobOfferDTO)
                .collect(Collectors.toList());
    }

    public List<JobOfferDTO> getActive() {
        return jobOfferRepository
                .findByActiveTrueAndDeadlineAfter(
                    LocalDate.now())
                .stream()
                .map(mapper::toJobOfferDTO)
                .collect(Collectors.toList());
    }

    public JobOfferDTO getById(Long id) {
        return mapper.toJobOfferDTO(
            jobOfferRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "JobOffer", "id", id)));
    }

    public JobOfferDTO update(Long id,
            JobOfferRequest request) {
        JobOffer offer = jobOfferRepository
                .findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "JobOffer", "id", id));

        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setDepartment(request.getDepartment());
        offer.setLocation(request.getLocation());
        offer.setContractType(request.getContractType());
        offer.setExperienceRequired(
            request.getExperienceRequired());
        offer.setSalaryRange(request.getSalaryRange());
        offer.setDeadline(request.getDeadline());

        log.info("Offre mise à jour : id={}", id);
        return mapper.toJobOfferDTO(
            jobOfferRepository.save(offer));
    }

    public void toggleActive(Long id) {
        JobOffer offer = jobOfferRepository
                .findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "JobOffer", "id", id));
        offer.setActive(!offer.isActive());
        jobOfferRepository.save(offer);
        log.info("Offre {} : id={}",
            offer.isActive() ? "activée" : "désactivée", id);
    }

    public void delete(Long id) {
        if (!jobOfferRepository.existsById(id))
            throw new ResourceNotFoundException(
                "JobOffer", "id", id);
        jobOfferRepository.deleteById(id);
        log.info("Offre supprimée : id={}", id);
    }
}
