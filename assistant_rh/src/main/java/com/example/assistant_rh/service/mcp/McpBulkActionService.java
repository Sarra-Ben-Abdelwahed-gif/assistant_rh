package com.example.assistant_rh.service.mcp;

import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.entity.User;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import com.example.assistant_rh.repository.UserRepository;
import com.example.assistant_rh.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class McpBulkActionService {

    private final LeaveRequestRepository
        leaveRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Approve all pending leave requests
    // that do not have conflicts
    public String approveAllSafe(
            String adminEmail,
            String comment) {
        User admin = userRepository
            .findByEmail(adminEmail)
            .orElseThrow(() ->
                new RuntimeException(
                    "Admin not found"));

        List<LeaveRequest> pending =
            leaveRepository.findByStatus(
                LeaveStatus.PENDING);

        if (pending.isEmpty())
            return "No pending requests.";

        int approved = 0;
        StringBuilder report =
            new StringBuilder();
        report.append(
            "Bulk approval results:\n\n");

        for (LeaveRequest leave : pending) {
            leave.setStatus(LeaveStatus.APPROVED);
            leave.setAdminComment(comment != null
                ? comment
                : "Automatically approved "
                + "via AI Assistant");
            leave.setApprovedBy(admin);
            leave.setApprovedAt(
                LocalDateTime.now());
            leaveRepository.save(leave);
            approved++;

            report.append(String.format(
                "✅ %s %s — %s → %s (%d d)\n",
                leave.getEmployee().getFirstName(),
                leave.getEmployee().getLastName(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getNumberOfDays()));

            // Notify employee by email
            try {
                emailService
                    .sendLeaveStatusNotification(
                        leave.getEmployee().getEmail(),
                        leave.getEmployee()
                            .getFirstName()
                            + " "
                            + leave.getEmployee()
                            .getLastName(),
                        "APPROVED",
                        "Approved via AI Assistant");
            } catch (Exception e) {
                log.warn("Email not sent: {}",
                    e.getMessage());
            }
        }

        report.append(String.format(
            "\n📊 Total: %d request(s) approved",
            approved));
        log.info("Bulk approve: {} leaves approved "
            + "by {}", approved, adminEmail);

        return report.toString();
    }

    // Preview pending leaves
    // (before confirmation)
    public String previewPendingApprovals() {
        List<LeaveRequest> pending =
            leaveRepository.findByStatus(
                LeaveStatus.PENDING);

        if (pending.isEmpty())
            return "No pending requests "
                + "at this moment.";

        StringBuilder sb = new StringBuilder(
            String.format(
                "📋 %d request(s) awaiting "
                + "approval:\n\n",
                pending.size()));

        pending.forEach(l -> sb.append(
            String.format(
                "• %s %s | %s | %s → %s | %d d\n",
                l.getEmployee().getFirstName(),
                l.getEmployee().getLastName(),
                l.getType(),
                l.getStartDate(),
                l.getEndDate(),
                l.getNumberOfDays())));

        sb.append("\n💬 Reply 'Approve all' "
            + "to validate them all, or "
            + "specify the ones to process "
            + "individually.");

        return sb.toString();
    }

    // Reject duplicate leave requests
    // (detected overlaps)
    public String rejectDuplicates(
            String adminEmail) {
        List<LeaveRequest> pending =
            leaveRepository.findByStatus(
                LeaveStatus.PENDING);

        int rejected = 0;
        for (LeaveRequest leave : pending) {
            boolean hasDuplicate = pending.stream()
                .anyMatch(other ->
                    !other.getId()
                        .equals(leave.getId())
                    && other.getEmployee().getId()
                        .equals(leave.getEmployee()
                            .getId())
                    && !other.getStartDate()
                        .isAfter(leave.getEndDate())
                    && !leave.getStartDate()
                        .isAfter(other.getEndDate()));

            if (hasDuplicate) {
                leave.setStatus(
                    LeaveStatus.REJECTED);
                leave.setAdminComment(
                    "Automatically rejected: "
                    + "overlap detected");
                leaveRepository.save(leave);
                rejected++;
            }
        }

        return rejected > 0
            ? rejected + " duplicate request(s)"
                + " automatically rejected."
            : "No duplicates detected.";
    }
}