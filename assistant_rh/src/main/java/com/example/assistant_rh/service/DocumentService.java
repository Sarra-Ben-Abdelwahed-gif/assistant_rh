package com.example.assistant_rh.service;

import com.example.assistant_rh.dto.response
    .DocumentDTO;
import com.example.assistant_rh.dto.response
    .DocumentUploadResponse;
import com.example.assistant_rh.entity.Document;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.User;

import com.example.assistant_rh.exception
    .FileUploadException;
import com.example.assistant_rh.exception
    .ResourceNotFoundException;
import com.example.assistant_rh.repository
    .DocumentRepository;
import com.example.assistant_rh.repository
    .EmployeeRepository;
import com.example.assistant_rh.repository
    .UserRepository;
import com.example.assistant_rh.config.MapperConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context
    .SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart
    .MultipartFile;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository
        documentRepository;
    private final EmployeeRepository
        employeeRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final MapperConfig mapper;

    // Allowed file types
    private static final List<String>
        ALLOWED_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats"
                + "-officedocument"
                + ".wordprocessingml.document",
            "image/jpeg",
            "image/png");

    private static final List<String>
        ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx",
            "jpg", "jpeg", "png");

    // Max size: 10 MB
    private static final long MAX_SIZE =
        10 * 1024 * 1024;

    // ── Main upload ──────────────────────
    public DocumentUploadResponse upload(
            MultipartFile file,
            Long employeeId,
            String category,
            String description) {

        // ── file validation ─────────────

        // 1. empty file
        if (file == null || file.isEmpty()) {
            return buildError(
                "No file selected. "
                + "Please choose a file.");
        }

        // 2. File size
        if (file.getSize() > MAX_SIZE) {
            long sizeMb = file.getSize()
                / (1024 * 1024);
            return buildError(
                "File too large : " + sizeMb
                + " MB. Maximum allowed : 10 MB.");
        }

        // 3. Type MIME
        String contentType =
            file.getContentType();
        if (contentType == null
                || !ALLOWED_TYPES.contains(
                    contentType)) {
            return buildError(
                "File type not allowed : "
                + contentType
                + ". Allowed types : "
                + "PDF, DOC, DOCX, JPG, PNG.");
        }

        // 4. file extension
        String originalName =
            file.getOriginalFilename();
        if (originalName == null
                || originalName.isBlank()) {
            return buildError(
                "Invalid file name.");
        }

        String extension = originalName
            .substring(
                originalName.lastIndexOf('.')
                + 1)
            .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(
                extension)) {
            return buildError(
                "Extension ." + extension
                + " not allowed. "
                + "Allowed : .pdf, .doc, "
                + ".docx, .jpg, .png");
        }

        // 5. dangerous file name
        if (originalName.contains("..")
                || originalName.contains("/")
                || originalName.contains("\\")) {
            return buildError(
                "Invalid file name.");
        }

        // ── Get employee ───────────────
        Employee employee = employeeRepository
            .findById(employeeId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Employee", "id",
                    employeeId));

        // ── Get current admin ────────
        String adminEmail =
            SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User uploader = userRepository
            .findByEmail(adminEmail)
            .orElse(null);

        // ── Upload to MinIO ─────────────────
        try {
            String minioKey = minioService
                .uploadFile(file, employeeId);

            // ── Create Document entity ───────
            Document document = Document.builder()
                .fileName(originalName)
                .fileType(extension.toUpperCase())
                .contentType(contentType)
                .fileSize(file.getSize())
                .minioKey(minioKey)
                .documentCategory(category)
                .description(description)
                .uploadedAt(LocalDateTime.now())
                .employee(employee)
                .uploadedBy(uploader)
                .build();

            documentRepository.save(document);

            // ── Generate download URL ─
            String downloadUrl = "";
            try {
                downloadUrl = minioService
                    .generateDownloadUrl(minioKey);
            } catch (Exception e) {
                log.warn("Download URL error : {}",
                    e.getMessage());
            }

            log.info("Document uploaded : {} "
                + "for employee id={}",
                originalName, employeeId);

            // ── success response ────────────────
            return DocumentUploadResponse.builder()
                .id(document.getId())
                .fileName(originalName)
                .fileType(extension.toUpperCase())
                .contentType(contentType)
                .fileSize(file.getSize())
                .fileSizeFormatted(
                    formatSize(file.getSize()))
                .documentCategory(category)
                .description(description)
                .uploadedAt(
                    document.getUploadedAt())
                .downloadUrl(downloadUrl)
                .success(true)
                .message("✅ Document '"
                    + originalName
                    + "' uploaded successfully ! ("
                    + formatSize(file.getSize())
                    + ")")
                .employeeName(
                    employee.getFirstName()
                    + " "
                    + employee.getLastName())
                .build();

        } catch (Exception e) {
            log.error("Upload failed : {}",
                e.getMessage());
            return buildError(
                "Upload failed : "
                + e.getMessage());
        }
    }

    // ── Get employee documents ──────────
    public List<DocumentDTO> getByEmployee(
            Long employeeId) {
        return documentRepository
            .findByEmployeeId(employeeId)
            .stream()
            .map(d -> {
                DocumentDTO dto =
                    mapper.toDocumentDTO(d);
                try {
                    dto.setDownloadUrl(
                        minioService
                            .generateDownloadUrl(
                                d.getMinioKey()));
                } catch (Exception e) {
                    log.warn("URL error : {}",
                        e.getMessage());
                }
                return dto;
            })
            .toList();
    }

    // ── download document ───────────────
    public DocumentDTO getDownloadUrl(
            Long docId) {
        Document doc = documentRepository
            .findById(docId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Document", "id", docId));
        DocumentDTO dto = mapper.toDocumentDTO(doc);
        try {
            dto.setDownloadUrl(
                minioService.generateDownloadUrl(
                    doc.getMinioKey()));
        } catch (Exception e) {
            throw new FileUploadException(
                e.getMessage());
        }
        return dto;
    }

    // ── delete document ─────────────────
    public void delete(Long docId) {
        Document doc = documentRepository
            .findById(docId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Document", "id", docId));
        try {
            minioService.deleteFile(
                doc.getMinioKey());
        } catch (Exception e) {
            log.warn("MinIO delete error : {}",
                e.getMessage());
        }
        documentRepository.delete(doc);
        log.info("Document deleted id={}",
            docId);
    }

    // ── private utilities ────────────────────
    private DocumentUploadResponse buildError(
            String message) {
        return DocumentUploadResponse.builder()
            .success(false)
            .message("❌ " + message)
            .build();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB",
                bytes / 1024.0);
        return String.format("%.1f MB",
            bytes / (1024.0 * 1024));
    }

    
}