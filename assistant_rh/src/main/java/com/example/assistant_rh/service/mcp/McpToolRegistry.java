package com.example.assistant_rh.service.mcp;

import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.LeaveType;
import com.example.assistant_rh.exception.BadRequestException;
import com.example.assistant_rh.exception.InsufficientLeaveBalanceException;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class McpToolRegistry {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final JobOfferRepository jobOfferRepository;

    // ══════════════════════════════════════════════
    // OUTIL 1 — Solde de congés
    // ══════════════════════════════════════════════
    public String getLeaveBalance(String email) {
        try {
            Employee emp = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "email", email));

            long pending = leaveRepository
                .findByEmployeeId(emp.getId())
                .stream()
                .filter(l -> l.getStatus()
                    == LeaveStatus.PENDING)
                .mapToLong(LeaveRequest::getNumberOfDays)
                .sum();

            return String.format(
                "Leave balance for %s %s :\n" +
                "- Available : %d jours\n" +
                "- Pending approval : %d jours\n" +
                "- If approved, remaining : %d jours",
                emp.getFirstName(),
                emp.getLastName(),
                emp.getAnnualLeaveBalance(),
                pending,
                Math.max(0,
                    emp.getAnnualLeaveBalance()
                    - (int) pending));
        } catch (Exception e) {
            return "Unable to retrieve balance : "
                + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 2 — Créer une demande de congé
    // ══════════════════════════════════════════════
    public String createLeaveRequest(
            String email,
            String startDateStr,
            String endDateStr,
            String typeStr,
            String reason) {
        try {
            Employee emp = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "email", email));

            LocalDate start =
                LocalDate.parse(startDateStr);
            LocalDate end =
                LocalDate.parse(endDateStr);

            if (end.isBefore(start))
                return "Error: End date must be after start date.";

            if (start.isBefore(LocalDate.now()))
                return "Error: Start date must be in the future.";

            long days = ChronoUnit.DAYS
                .between(start, end) + 1;

            LeaveType type;
            try {
                type = LeaveType.valueOf(
                    typeStr.toUpperCase());
            } catch (Exception e) {
                return "Invalid type. Accepted types: ANNUAL, SICK, MATERNITY, PATERNITY, UNPAID";
            }

            if (type == LeaveType.ANNUAL
                    && emp.getAnnualLeaveBalance()
                    < days)
                return String.format(
                    "Insufficient balance. You have %d day(s) available but you are requesting %d.",
                    emp.getAnnualLeaveBalance(),
                    days);

            boolean overlap = leaveRepository
                .existsOverlappingLeave(
                    emp.getId(), start, end);
            if (overlap)
                return "You already have a request "
                    + "for this period.";

            LeaveRequest leave = LeaveRequest.builder()
                    .employee(emp)
                    .startDate(start)
                    .endDate(end)
                    .type(type)
                    .reason(reason)
                    .build();

            leaveRepository.save(leave);
            log.info("MCP: congé créé pour {}",
                email);

            return String.format(
                "✅ Leave request created successfully!\n"
                + "- Type: %s\n"
                + "- From: %s to %s\n"
                + "- Duration: %d day(s)\n"
                + "- Reason: %s\n"
                + "- Status: PENDING approval\n"
                + "Your HR manager will be notified.",
                type, start, end, days,
                reason != null ? reason : "Not specified");

        } catch (DateTimeParseException e) {
            return "Invalid date format. Use YYYY-MM-DD (ex: 2026-07-15)";
        } catch (Exception e) {
            return "Error occurred while creating the request: "
                + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 3 — Mes demandes de congé
    // ══════════════════════════════════════════════
    public String getMyLeaves(String email) {
        try {
            Employee emp = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "email", email));

            List<LeaveRequest> leaves =
                leaveRepository.findByEmployeeId(
                    emp.getId());

            if (leaves.isEmpty())
                return "You have no leave requests.";

            StringBuilder sb = new StringBuilder(
                "Your leave requests :\n\n");

            leaves.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null)
                        return 1;
                    if (b.getCreatedAt() == null)
                        return -1;
                    return b.getCreatedAt()
                        .compareTo(a.getCreatedAt());
                })
                .limit(5)
                .forEach(l -> sb.append(
                    String.format(
                        "- %s | %s → %s | %d day(s)"
                        + " | Status: %s\n",
                        l.getType(),
                        l.getStartDate(),
                        l.getEndDate(),
                        l.getNumberOfDays(),
                        l.getStatus())));

            return sb.toString();

        } catch (Exception e) {
            return "Error : " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 4 — Mes documents
    // ══════════════════════════════════════════════
    public String getMyDocuments(String email) {
        try {
            Employee emp = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "email", email));

            var docs = documentRepository
                .findByEmployeeId(emp.getId());

            if (docs.isEmpty())
                return "You have no documents available.";

            StringBuilder sb = new StringBuilder(
                "Your available HR documents :\n\n");
            docs.forEach(d -> sb.append(
                String.format("- [ID:%d] %s | %s"
                    + " | %s\n",
                    d.getId(),
                    d.getFileName(),
                    d.getDocumentCategory(),
                    d.getUploadedAt() != null
                        ? d.getUploadedAt()
                            .toLocalDate()
                        : "unknown date")));

            sb.append("\nTo download a document, go to My Documents.");

            return sb.toString();

        } catch (Exception e) {
            return "Error : " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 5 — Mon profil employé
    // ══════════════════════════════════════════════
    public String getMyProfile(String email) {
        try {
            Employee emp = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "email", email));

            return String.format(
                "Your profile :\n"
                + "- Name : %s %s\n"
                + "- Email : %s\n"
                + "- Department : %s\n"
                + "- Position : %s\n"
                + "- Hire Date : %s\n"
                + "- Status : %s\n"
                + "- Leave Balance : %d days\n"
                + "- Seniority : %d year(s)",
                emp.getFirstName(),
                emp.getLastName(),
                emp.getEmail(),
                emp.getDepartment(),
                emp.getPosition(),
                emp.getHireDate(),
                emp.getStatus(),
                emp.getAnnualLeaveBalance(),
                emp.getHireDate() != null
                    ? (int) ChronoUnit.YEARS.between(
                        emp.getHireDate(),
                        LocalDate.now())
                    : 0);

        } catch (Exception e) {
            return "Error : " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 6 — Offres d'emploi disponibles
    //           (pour candidats)
    // ══════════════════════════════════════════════
    public String getActiveJobs() {
        try {
            var jobs = jobOfferRepository
                .findByActiveTrueAndDeadlineAfter(
                    LocalDate.now());

            if (jobs.isEmpty())
                return "No job offers "
                    + "currently available.";

            StringBuilder sb = new StringBuilder(
                "Available job offers :\n\n");
            jobs.forEach(j -> sb.append(
                String.format(
                    "- [ID:%d] %s | %s | %s"
                    + " | Deadline: %s\n",
                    j.getId(),
                    j.getTitle(),
                    j.getDepartment(),
                    j.getContractType() != null
                        ? j.getContractType()
                        : "N/A",
                    j.getDeadline())));

            return sb.toString();

        } catch (Exception e) {
            return "Error : " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 7 — Mes candidatures (pour candidats)
    // ══════════════════════════════════════════════
    public String getMyCandidatures(String email) {
        try {
            var apps = applicationRepository
                .findByCandidateEmail(email);

            if (apps.isEmpty())
                return "You haven't applied to "
                    + "any job offers.";

            StringBuilder sb = new StringBuilder(
                "Your applications :\n\n");
            apps.forEach(a -> sb.append(
                String.format(
                    "- %s | Status: %s | %s\n",
                    a.getJobOffer() != null
                        ? a.getJobOffer().getTitle()
                        : "Unknown offer",
                    a.getStatus(),
                    a.getAppliedAt() != null
                        ? a.getAppliedAt()
                            .toLocalDate()
                        : "Unknown date")));

            return sb.toString();

        } catch (Exception e) {
            return "Error : " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 8 — Statistiques RH (pour admin)
    // ══════════════════════════════════════════════
    public String getHrStats() {
        try {
            long totalEmp =
                employeeRepository.count();
            long activeEmp =
                employeeRepository.countByStatus(
                    com.example.assistant_rh
                        .enums.EmployeeStatus.ACTIVE);
            long pendingLeaves =
                leaveRepository.countByStatus(
                    LeaveStatus.PENDING);
            long totalApps =
                applicationRepository.count();
            long activeJobs =
                jobOfferRepository.countByActiveTrue();

            return String.format(
                "Real-time HR Statistics :\n\n"
                + "👥 Total Employees : %d\n"
                + "✅ Active Employees : %d\n"
                + "⏳ Pending Leaves : %d\n"
                + "💼 Active Job Offers : %d\n"
                + "📋 Total Applications : %d",
                totalEmp, activeEmp,
                pendingLeaves, activeJobs,
                totalApps);

        } catch (Exception e) {
            return "Error stats : " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 9 — Chercher un employé (pour admin)
    // ══════════════════════════════════════════════
    public String searchEmployee(String query) {
        try {
            String q = query.toLowerCase();
            var results = employeeRepository
                .findAll()
                .stream()
                .filter(e ->
                    (e.getFirstName() != null
                        && e.getFirstName()
                        .toLowerCase().contains(q))
                    || (e.getLastName() != null
                        && e.getLastName()
                        .toLowerCase().contains(q))
                    || (e.getDepartment() != null
                        && e.getDepartment()
                        .toLowerCase().contains(q))
                    || (e.getPosition() != null
                        && e.getPosition()
                        .toLowerCase().contains(q))
                    || (e.getEmail() != null
                        && e.getEmail()
                        .toLowerCase().contains(q)))
                .limit(5)
                .collect(Collectors.toList());

            if (results.isEmpty())
                return "no employee found "
                    + "for : " + query;

            StringBuilder sb = new StringBuilder(
                "Employees found :\n\n");
            results.forEach(e -> sb.append(
                String.format(
                    "- %s %s | %s | %s | %s\n",
                    e.getFirstName(),
                    e.getLastName(),
                    e.getEmail(),
                    e.getDepartment(),
                    e.getStatus())));

            return sb.toString();

        } catch (Exception e) {
            return "Search error : "
                + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════
    // OUTIL 10 — Congés en attente (pour admin)
    // ══════════════════════════════════════════════
    public String getPendingLeaves() {
        try {
            var pending = leaveRepository
                .findByStatus(LeaveStatus.PENDING);

            if (pending.isEmpty())
                return "No pending leave requests ";

            StringBuilder sb = new StringBuilder(
                String.format(
                    "%d pending request(s) :\n\n",
                    pending.size()));

            pending.forEach(l -> sb.append(
                String.format(
                    "- %s %s | %s | %s → %s"
                    + " | %d day(s)\n",
                    l.getEmployee().getFirstName(),
                    l.getEmployee().getLastName(),
                    l.getType(),
                    l.getStartDate(),
                    l.getEndDate(),
                    l.getNumberOfDays())));

            return sb.toString();

        } catch (Exception e) {
            return "Error : " + e.getMessage();
        }
    }
   

    private final McpCalendarService calendarService;
    private final McpBulkActionService bulkService;
    private final McpShadowAiService shadowAiService;
    private final McpProactiveAlertService alertService;

    // ══════════════════════════════════════════════
    // TOOL 11 — Team Calendar
    // ══════════════════════════════════════════════
    public String getTeamCalendar(
            String email, int days) {
        try {
            Employee emp = employeeRepository
            .findByEmail(email)
            .orElseThrow();
        if (emp.getDepartment() == null)
            return "Department not defined.";
        return calendarService
            .getDepartmentCalendar(
                emp.getDepartment(), days);
    } catch (Exception e) {
        return "Error: " + e.getMessage();
    }
}

    // ══════════════════════════════════════════════
    // TOOL 12 — Proactive Alerts (admin)
    // ══════════════════════════════════════════════
    public String getProactiveAlerts() {
        return alertService.getAlertsSummary();
    }

    // ══════════════════════════════════════════════
    // TOOL 13 — Turnover Report (admin)
    // ══════════════════════════════════════════════
    public String getTurnoverRisk() {
         return shadowAiService.analyzeTurnoverRisk();
    }

    // ══════════════════════════════════════════════
    // TOOL 14 — Executive Question (admin)
    // ══════════════════════════════════════════════
    public String answerExecutiveQuestion(
            String question) {
        return shadowAiService
            .getExecutiveInsights(question);
    }

    // ══════════════════════════════════════════════
    // TOOL 15 — Bulk Approvals Preview
    // ══════════════════════════════════════════════
    public String previewBulkApproval() {
        return bulkService.previewPendingApprovals();
    }

    // ══════════════════════════════════════════════
    // TOOL 16 — Approve All (admin)
    // ══════════════════════════════════════════════
    public String approveAll(
            String adminEmail, String comment) {
        return bulkService.approveAllSafe(
            adminEmail, comment);
    }
}
