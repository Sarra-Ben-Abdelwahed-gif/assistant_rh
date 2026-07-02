package com.example.assistant_rh.service.mcp;

import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.LeaveType;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpProactiveAlertService {

    private final LeaveRequestRepository
        leaveRepository;
    private final EmployeeRepository
        employeeRepository;

    // Store generated proactive alerts
    private final List<ProactiveAlert> alerts =
        new CopyOnWriteArrayList<>();

    // Run verification checks every hour
    @Scheduled(cron = "0 0 * * * *")
    public void generateProactiveAlerts() {
        alerts.clear();
        checkPendingLeavesTooLong();
        checkBurnoutRisk();
        checkTeamAbsenceRate();
        checkUpcomingDeadlines();
        log.info("Proactive alerts: {} generated",
            alerts.size());
    }

    // Leave requests pending for more than 3 days
    private void checkPendingLeavesTooLong() {
        List<LeaveRequest> pending =
            leaveRepository.findByStatus(
                LeaveStatus.PENDING);

        long longPending = pending.stream()
            .filter(l -> l.getCreatedAt() != null
                && ChronoUnit.DAYS.between(
                    l.getCreatedAt(),
                    LocalDateTime.now()) > 3)
            .count();

        if (longPending > 0)
            alerts.add(new ProactiveAlert(
                AlertType.ACTION_REQUIRED,
                String.format(
                    "⚠️ %d leave request(s) "
                    + "pending for more than "
                    + "3 days. Would you like "
                    + "me to approve them "
                    + "all at once?",
                    longPending),
                "PENDING_LEAVES_TOO_LONG",
                LocalDate.now()));
    }

    // Burnout Risk — no leave requests taken
    // for more than 6 months
    private void checkBurnoutRisk() {
        List<Employee> employees =
            employeeRepository.findAll();
        List<String> atRisk = new ArrayList<>();

        for (Employee emp : employees) {
            List<LeaveRequest> approved =
                leaveRepository
                    .findByEmployeeId(emp.getId())
                    .stream()
                    .filter(l ->
                        l.getStatus()
                            == LeaveStatus.APPROVED)
                    .toList();

            boolean noRecentLeave =
                approved.isEmpty()
                || approved.stream().noneMatch(l ->
                    l.getEndDate().isAfter(
                        LocalDate.now()
                            .minusMonths(6)));

            if (noRecentLeave)
                atRisk.add(emp.getFirstName()
                    + " " + emp.getLastName());
        }

        if (!atRisk.isEmpty())
            alerts.add(new ProactiveAlert(
                AlertType.WELLBEING,
                String.format(
                    "🔴 Burnout risk detected: "
                    + "%d employee(s) have not "
                    + "taken leave for more "
                    + "than 6 months: %s",
                    atRisk.size(),
                    String.join(", ", atRisk)),
                "BURNOUT_RISK",
                LocalDate.now()));
    }

    // High absence rate calculation inside a specific department
    private void checkTeamAbsenceRate() {
        List<String> depts = employeeRepository
            .findAllDepartments();

        for (String dept : depts) {
            List<Employee> team =
                employeeRepository
                    .findByDepartment(dept);
            if (team.size() < 2) continue;

            long absent = team.stream()
                .filter(e ->
                    leaveRepository
                        .findByEmployeeId(e.getId())
                        .stream()
                        .anyMatch(l ->
                            l.getStatus()
                                == LeaveStatus.APPROVED
                            && !l.getStartDate()
                                .isAfter(
                                    LocalDate.now())
                            && !l.getEndDate()
                                .isBefore(
                                    LocalDate.now())))
                .count();

            double rate =
                (double) absent / team.size()
                * 100;

            if (rate >= 40)
                alerts.add(new ProactiveAlert(
                    AlertType.TEAM_ALERT,
                    String.format(
                        "👥 Department %s: %.0f%%"
                        + " of the team is absent"
                        + " today (%d/%d). "
                        + "Operational risk.",
                        dept, rate,
                        absent, team.size()),
                    "HIGH_ABSENCE_RATE",
                    LocalDate.now()));
        }
    }

    // Monitor crucial milestones and deadlines
    private void checkUpcomingDeadlines() {
        // Work anniversaries occurring today
        List<Employee> employees =
            employeeRepository.findAll();
        LocalDate today = LocalDate.now();

        employees.stream()
            .filter(e ->
                e.getHireDate() != null
                && e.getHireDate().getMonth()
                    == today.getMonth()
                && e.getHireDate().getDayOfMonth()
                    == today.getDayOfMonth())
            .forEach(e -> {
                long years = ChronoUnit.YEARS
                    .between(e.getHireDate(), today);
                alerts.add(new ProactiveAlert(
                    AlertType.CELEBRATION,
                    String.format(
                        "🎉 Today is the work "
                        + "anniversary of "
                        + "%s %s! %d year(s) "
                        + "with the company.",
                        e.getFirstName(),
                        e.getLastName(), years),
                    "HIRE_ANNIVERSARY",
                    today));
            });
    }

    // Retrieve active alerts snapshot list
    public List<ProactiveAlert> getAlerts() {
        return List.copyOf(alerts);
    }

    // Alert details formatted text summary outputted to chatbot view
    public String getAlertsSummary() {
        if (alerts.isEmpty())
            return "✅ No active alerts "
                + "at this moment.";

        StringBuilder sb = new StringBuilder(
            String.format(
                "🔔 %d active alert(s):\n\n",
                alerts.size()));

        alerts.forEach(a ->
            sb.append("• ").append(a.message())
                .append("\n\n"));

        return sb.toString();
    }

    // Alert structural categorizations definitions
    public enum AlertType {
        ACTION_REQUIRED,
        WELLBEING,
        TEAM_ALERT,
        CELEBRATION,
        INFO
    }

    public record ProactiveAlert(
        AlertType type,
        String message,
        String code,
        LocalDate date) {}
}
