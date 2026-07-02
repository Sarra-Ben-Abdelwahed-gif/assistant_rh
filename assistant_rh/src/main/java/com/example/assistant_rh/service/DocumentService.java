package com.example.assistant_rh.service;

import com.example.assistant_rh.config.MapperConfig;
import com.example.assistant_rh.dto.response.DocumentDTO;
import com.example.assistant_rh.entity.Document;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.exception.FileUploadException;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.DocumentRepository;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final MapperConfig mapper;

    
    public DocumentDTO uploadDocument(MultipartFile file,
                                      Long employeeId, String category,
                                      String description) {
        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee", "id", employeeId));

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User uploader = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User", "email", email));

        String minioKey;
        try {
            minioKey = minioService.uploadFile(file);
        } catch (Exception e) {
            throw new FileUploadException(e.getMessage());
        }

        String fileName = file.getOriginalFilename();
        String fileType = fileName != null
                && fileName.contains(".")
                ? fileName.substring(
                fileName.lastIndexOf('.') + 1)
                .toUpperCase()
                : "UNKNOWN";

        Document doc = Document.builder()
                .minioKey(minioKey)
                .documentCategory(category)
                .description(description)
                .employee(employee)
                .uploadedBy(uploader)
                .build();

        DocumentDTO dto = mapper.toDocumentDTO(
                documentRepository.save(doc));

        try {
            dto.setDownloadUrl(
                    minioService.generateDownloadUrl(minioKey));
        } catch (Exception ignored) {}

        log.info("Document uploaded: {} for employee id={}",
                fileName, employeeId);
        return dto;
    }

    
    public Page<DocumentDTO> getAll(Pageable pageable) {
        return documentRepository.findAll(pageable)
                .map(d -> {
                    DocumentDTO dto = mapper.toDocumentDTO(d);
                    try {
                        dto.setDownloadUrl(
                                minioService.generateDownloadUrl(d.getMinioKey()));
                    } catch (Exception ignored) {}
                    return dto;
                });
    }

    public List<DocumentDTO> getByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException(
                    "Employee", "id", employeeId);
        }

        return documentRepository
                .findByEmployeeId(employeeId)
                .stream()
                .map(d -> {
                    DocumentDTO dto = mapper.toDocumentDTO(d);
                    try {
                        dto.setDownloadUrl(
                                minioService.generateDownloadUrl(
                                        d.getMinioKey()));
                    } catch (Exception ignored) {}
                    return dto;
                })
                .collect(Collectors.toList());
    }

    
    public DocumentDTO downloadDocument(Long id) {
        Document doc = documentRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document", "id", id));
        DocumentDTO dto = mapper.toDocumentDTO(doc);
        try {
            dto.setDownloadUrl(
                    minioService.generateDownloadUrl(
                            doc.getMinioKey()));
        } catch (Exception e) {
            throw new FileUploadException(
                    "Unable to generate URL : "
                            + e.getMessage());
        }
        return dto;
    }

    
    public DocumentDTO getById(Long id) {
        Document doc = documentRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document", "id", id));
        DocumentDTO dto = mapper.toDocumentDTO(doc);
        try {
            dto.setDownloadUrl(
                    minioService.generateDownloadUrl(doc.getMinioKey()));
        } catch (Exception ignored) {}
        return dto;
    }

    
    public void deleteDocument(Long id) {
        Document doc = documentRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document", "id", id));
        try {
            minioService.deleteFile(doc.getMinioKey());
        } catch (Exception e) {
            log.warn("MinIO file not deleted: {}",
                    e.getMessage());
        }
        documentRepository.delete(doc);
        log.info("Document deleted: id={}", id);
    }
}