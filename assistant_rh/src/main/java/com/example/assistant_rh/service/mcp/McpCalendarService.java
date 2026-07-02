package com.example.assistant_rh.service.mcp;

import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpCalendarService {

    private final LeaveRequestRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    // Analyze team scheduling conflicts before processing a leave request
    public ConflictAnalysis analyzeConflicts(
            String requestingEmail,
            LocalDate startDate,
            LocalDate endDate) {

        Employee requester = employeeRepository
            .findByEmail(requestingEmail)
            .orElse(null);

        if (requester == null || requester.getDepartment() == null)
            return new ConflictAnalysis(false, List.of(), 0);

        String department = requester.getDepartment();

        // Retrieve all team members in the same department who are absent during this period
        List<Employee> teamMembers = employeeRepository
            .findByDepartment(department)
            .stream()
            .filter(e -> !e.getEmail().equals(requestingEmail))
            .collect(Collectors.toList());

        List<ConflictInfo> conflicts = teamMembers.stream()
            .flatMap(member -> leaveRepository
                .findByEmployeeId(member.getId())
                .stream()
                .filter(l -> l.getStatus() != LeaveStatus.REJECTED
                        && datesOverlap(l.getStartDate(), l.getEndDate(), startDate, endDate))
                .map(l -> new ConflictInfo(
                    member.getFirstName() + " " + member.getLastName(),
                    member.getPosition(),
                    l.getStartDate(),
                    l.getEndDate(),
                    l.getStatus())))
            .collect(Collectors.toList());

        // Calculate the team absence overlap rate
        int teamSize = teamMembers.size() + 1;
        double absenceRate = teamSize > 0
            ? (double) conflicts.size() / teamSize * 100
            : 0;

        return new ConflictAnalysis(
            !conflicts.isEmpty(),
            conflicts,
            (int) absenceRate);
    }

    // Identify upcoming conflict-free date blocks as alternative recommendations
    public String suggestAlternativeDates(
            String email,
            LocalDate startDate,
            int durationDays) {

        StringBuilder sb = new StringBuilder();
        LocalDate suggested = startDate.plusWeeks(1);

        for (int attempt = 0; attempt < 4; attempt++) {
            LocalDate end = suggested.plusDays(durationDays - 1);
            ConflictAnalysis analysis = analyzeConflicts(email, suggested, end);

            if (!analysis.hasConflict()) {
                sb.append(String.format(
                    "✅ Conflict-free alternative period found: %s → %s",
                    suggested, end));
                return sb.toString();
            }
            suggested = suggested.plusWeeks(1);
        }

        return "No conflict-free alternative slots found within the next 4 weeks. "
            + "I highly recommend discussing scheduling options directly with your manager.";
    }

    // Format a structured summary of active department absences
    public String getDepartmentCalendar(String department, int nextDays) {

        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(nextDays);

        List<Employee> team = employeeRepository.findByDepartment(department);

        if (team.isEmpty())
            return "No registered employees found for this department.";

        StringBuilder sb = new StringBuilder(
            String.format(
                "Absence Calendar — %s (Next %d Days):\n\n",
                department, nextDays));

        boolean found = false;
        for (Employee emp : team) {
            List<LeaveRequest> leaves = leaveRepository
                .findByEmployeeId(emp.getId())
                .stream()
                .filter(l -> l.getStatus() != LeaveStatus.REJECTED
                        && l.getEndDate().isAfter(today)
                        && l.getStartDate().isBefore(until))
                .collect(Collectors.toList());

            if (!leaves.isEmpty()) {
                found = true;
                for (LeaveRequest l : leaves) {
                    sb.append(String.format(
                        "• %s %s — %s → %s (%d days) [%s]\n",
                        emp.getFirstName(),
                        emp.getLastName(),
                        l.getStartDate(),
                        l.getEndDate(),
                        l.getNumberOfDays(),
                        l.getStatus()));
                }
            }
        }

        if (!found)
            sb.append("No absences scheduled during this timeframe. ✅");

        return sb.toString();
    }

    private boolean datesOverlap(
            LocalDate s1, LocalDate e1,
            LocalDate s2, LocalDate e2) {
        return !s1.isAfter(e2) && !s2.isAfter(e1);
    }

    // ── Inner Records ─────────────────────────────────
    public record ConflictAnalysis(
        boolean hasConflict,
        List<ConflictInfo> conflicts,
        int absenceRatePercent) {}

    public record ConflictInfo(
        String employeeName,
        String position,
        LocalDate startDate,
        LocalDate endDate,
        LeaveStatus status) {}
}