package com.example.assistant_rh.config;

import com.example.assistant_rh.dto.response.*;
import com.example.assistant_rh.entity.*;
import org.springframework.stereotype.Component;

@Component
public class MapperConfig {

    public EmployeeDTO toEmployeeDTO(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(e.getId());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setDepartment(e.getDepartment());
        dto.setPosition(e.getPosition());
        dto.setAddress(e.getAddress());
        dto.setHireDate(e.getHireDate());
        dto.setBirthDate(e.getBirthDate());
        dto.setStatus(e.getStatus());
        dto.setAnnualLeaveBalance(e.getAnnualLeaveBalance());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    public LeaveRequestDTO toLeaveDTO(LeaveRequest l) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(l.getId());
        dto.setEmployeeId(l.getEmployee().getId());
        dto.setEmployeeFullName(
            l.getEmployee().getFirstName()
            + " " + l.getEmployee().getLastName());
        dto.setEmployeeDepartment(
            l.getEmployee().getDepartment());
        dto.setStartDate(l.getStartDate());
        dto.setEndDate(l.getEndDate());
        dto.setType(l.getType());
        dto.setStatus(l.getStatus());
        dto.setReason(l.getReason());
        dto.setAdminComment(l.getAdminComment());
        dto.setNumberOfDays(l.getNumberOfDays());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setApprovedAt(l.getApprovedAt());
        if (l.getApprovedBy() != null)
            dto.setApprovedByEmail(
                l.getApprovedBy().getEmail());
        return dto;
    }

    public DocumentDTO toDocumentDTO(Document d) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(d.getId());
        dto.setFileName(d.getFileName());
        dto.setFileType(d.getFileType());
        dto.setContentType(d.getContentType());
        dto.setFileSize(d.getFileSize());
        dto.setDocumentCategory(d.getDocumentCategory());
        dto.setDescription(d.getDescription());
        dto.setUploadedAt(d.getUploadedAt());
        if (d.getEmployee() != null) {
            dto.setEmployeeId(d.getEmployee().getId());
            dto.setEmployeeFullName(
                d.getEmployee().getFirstName()
                + " " + d.getEmployee().getLastName());
        }
        if (d.getUploadedBy() != null)
            dto.setUploadedByEmail(
                d.getUploadedBy().getEmail());
        return dto;
    }

    public JobOfferDTO toJobOfferDTO(JobOffer j) {
        JobOfferDTO dto = new JobOfferDTO();
        dto.setId(j.getId());
        dto.setTitle(j.getTitle());
        dto.setDescription(j.getDescription());
        dto.setDepartment(j.getDepartment());
        dto.setLocation(j.getLocation());
        dto.setContractType(j.getContractType());
        dto.setExperienceRequired(j.getExperienceRequired());
        dto.setSalaryRange(j.getSalaryRange());
        dto.setDeadline(j.getDeadline());
        dto.setActive(j.isActive());
        dto.setCreatedAt(j.getCreatedAt());
        dto.setApplicationCount(
            j.getApplications() != null
                ? j.getApplications().size() : 0);
        return dto;
    }

    public ApplicationDTO toApplicationDTO(Application a) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(a.getId());
        dto.setCandidateName(a.getCandidateName());
        dto.setCandidateEmail(a.getCandidateEmail());
        dto.setCandidatePhone(a.getCandidatePhone());
        dto.setCvFileName(a.getCvFileName());
        dto.setCoverLetter(a.getCoverLetter());
        dto.setStatus(a.getStatus());
        dto.setScore(a.getScore());
        dto.setAiAnalysis(a.getAiAnalysis());
        dto.setHrComment(a.getHrComment());
        dto.setAppliedAt(a.getAppliedAt());
        if (a.getJobOffer() != null) {
            dto.setJobOfferId(a.getJobOffer().getId());
            dto.setJobOfferTitle(a.getJobOffer().getTitle());
        }
        return dto;
    }
}