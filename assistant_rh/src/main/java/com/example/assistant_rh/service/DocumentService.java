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

// Les deux imports pour la pagination
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

    // Renommé en uploadDocument pour correspondre au Controller
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

        log.info("Document uploadé : {} pour employé id={}",
                fileName, employeeId);
        return dto;
    }

    // Méthode de pagination générale
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

    // Renommé en downloadDocument pour correspondre au Controller
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
                    "Impossible de générer l'URL : "
                            + e.getMessage());
        }
        return dto;
    }

    // Ajout de la méthode getById demandée par ton contrôleur à la ligne 70
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

    // Renommé en deleteDocument pour correspondre au Controller
    public void deleteDocument(Long id) {
        Document doc = documentRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document", "id", id));
        try {
            minioService.deleteFile(doc.getMinioKey());
        } catch (Exception e) {
            log.warn("Fichier MinIO non supprimé : {}",
                    e.getMessage());
        }
        documentRepository.delete(doc);
        log.info("Document supprimé : id={}", id);
    }
}