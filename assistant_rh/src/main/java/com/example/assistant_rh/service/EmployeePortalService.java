package com.example.assistant_rh.service;

import com.example.assistant_rh.config.MapperConfig;
import com.example.assistant_rh.dto.response.DocumentDTO;
import com.example.assistant_rh.dto.response.EmployeeDashboardDTO;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.exception.FileUploadException;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.exception.UnauthorizedAccessException;
import com.example.assistant_rh.repository.DocumentRepository;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeePortalService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRepository;
    private final DocumentRepository documentRepository;
    private final MinioService minioService;
    private final MapperConfig mapper;

    // Tableau de bord de l'employé connecté
    public EmployeeDashboardDTO getMyDashboard() {
        String email = getCurrentEmail();
        Employee emp = getEmployeeByEmail(email);

        EmployeeDashboardDTO dto =
            new EmployeeDashboardDTO();
        dto.setId(emp.getId());
        dto.setFirstName(emp.getFirstName());
        dto.setLastName(emp.getLastName());
        dto.setEmail(emp.getEmail());
        dto.setPhone(emp.getPhone());
        dto.setDepartment(emp.getDepartment());
        dto.setPosition(emp.getPosition());
        dto.setHireDate(emp.getHireDate());
        dto.setStatus(emp.getStatus());
        dto.setAnnualLeaveBalance(
            emp.getAnnualLeaveBalance());

        var leaves = leaveRepository
            .findByEmployeeId(emp.getId());

        dto.setPendingLeaves(leaves.stream()
            .filter(l -> l.getStatus()
                == LeaveStatus.PENDING).count());
        dto.setApprovedLeaves(leaves.stream()
            .filter(l -> l.getStatus()
                == LeaveStatus.APPROVED).count());
        dto.setRejectedLeaves(leaves.stream()
            .filter(l -> l.getStatus()
                == LeaveStatus.REJECTED).count());

        dto.setTotalDocuments(
            documentRepository.countByEmployeeId(
                emp.getId()));

        dto.setRecentLeaves(leaves.stream()
            .sorted((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt()
                    .compareTo(a.getCreatedAt());
            })
            .limit(5)
            .map(mapper::toLeaveDTO)
            .collect(Collectors.toList()));

        return dto;
    }

    // Mes documents
    public List<DocumentDTO> getMyDocuments() {
        String email = getCurrentEmail();
        Employee emp = getEmployeeByEmail(email);

        return documentRepository
            .findByEmployeeId(emp.getId())
            .stream()
            .map(d -> {
                DocumentDTO dto =
                    mapper.toDocumentDTO(d);
                try {
                    dto.setDownloadUrl(
                        minioService.generateDownloadUrl(
                            d.getMinioKey()));
                } catch (Exception ignored) {}
                return dto;
            })
            .collect(Collectors.toList());
    }

    // Télécharger un de mes documents
    public DocumentDTO getMyDocumentDownload(
            Long docId) {
        String email = getCurrentEmail();
        Employee emp = getEmployeeByEmail(email);

        var doc = documentRepository
            .findById(docId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Document", "id", docId));

        // ✅ Vérifier que le document appartient
        // à l'employé connecté
        if (!doc.getEmployee().getId()
                .equals(emp.getId()))
            throw new UnauthorizedAccessException();

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

    // ─── Méthodes privées ────────────────────────────
    private Employee getEmployeeByEmail(String email) {
        return employeeRepository
            .findByEmail(email)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Employee", "email", email));
    }

    private String getCurrentEmail() {
        return SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();
    }
}