package com.example.assistant_rh.service.mcp;

import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.enums.EmployeeStatus;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.LeaveType;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import com.example.assistant_rh.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpShadowAiService {

    private final EmployeeRepository
        employeeRepository;
    private final LeaveRequestRepository
        leaveRepository;
    private final GeminiService geminiService;

    // Turnover risk analysis
    // by department
    public String analyzeTurnoverRisk() {
        List<Employee> employees =
            employeeRepository.findAll();

        Map<String, DepartmentRisk> riskByDept =
            new HashMap<>();

        for (Employee emp : employees) {
            if (emp.getDepartment() == null)
                continue;

            String dept = emp.getDepartment();
            riskByDept.putIfAbsent(dept,
                new DepartmentRisk(dept));

            DepartmentRisk risk =
                riskByDept.get(dept);
            risk.totalEmployees++;

            List<LeaveRequest> empLeaves =
                leaveRepository.findByEmployeeId(
                    emp.getId());

            // Risk signals
            // 1. Too many sick leaves
            long sickLeaves = empLeaves.stream()
                .filter(l ->
                    l.getType() == LeaveType.SICK
                    && l.getStatus()
                        != LeaveStatus.REJECTED)
                .count();
            if (sickLeaves >= 3)
                risk.highRiskEmployees++;

            // 2. No leaves taken for a long time
            // (burnout risk)
            boolean noRecentLeave =
                empLeaves.stream()
                    .filter(l ->
                        l.getStatus()
                            == LeaveStatus.APPROVED)
                    .noneMatch(l ->
                        l.getEndDate().isAfter(
                            LocalDate.now()
                                .minusMonths(6)));
            if (noRecentLeave
                    && empLeaves.size() > 0)
                risk.burnoutRisk++;

            // 3. Low seniority < 1 year
            if (emp.getHireDate() != null) {
                long months = ChronoUnit.MONTHS
                    .between(emp.getHireDate(),
                        LocalDate.now());
                if (months < 12)
                    risk.newEmployees++;
            }

            // 4. Inactive or long leave status
            if (emp.getStatus()
                    == EmployeeStatus.ON_LEAVE)
                risk.onLeaveCount++;
        }

        // Build the raw data report for Gemini
        StringBuilder rawData = new StringBuilder(
            "HR Data by Department:\n\n");

        riskByDept.values().stream()
            .sorted(Comparator.comparingDouble(
                r -> -(double) r.highRiskEmployees
                    / Math.max(r.totalEmployees, 1)))
            .forEach(r -> rawData.append(
                String.format(
                    "Department: %s\n"
                    + "  Total: %d employees\n"
                    + "  High Risk: %d\n"
                    + "  Burnout Risk: %d\n"
                    + "  New Hires (<1 year): %d\n"
                    + "  On Long Leave: %d\n\n",
                    r.department,
                    r.totalEmployees,
                    r.highRiskEmployees,
                    r.burnoutRisk,
                    r.newEmployees,
                    r.onLeaveCount)));

        String systemPrompt = """
            You are a strategic HR analyst
            for corporate management.
            Analyze this data and generate
            a concise executive report.
            
            Format:
            ## 📊 Turnover Risk Report
            
            ### 🔴 High-Risk Department(s)
            [department + risk score + reasons]
            
            ### 🟡 Warning Signals
            [list of detected warning signs]
            
            ### 💡 Strategic Recommendations
            [3 concrete priority actions]
            
            ### 📈 Observed Trends
            [macro-level analysis]
            
            Be direct, factual, and actionable.
            Maximum 300 words.
            """;

        String report = geminiService.chat(
            systemPrompt, rawData.toString());

        log.info("Shadow AI: turnover risk report generated");
        return report;
    }

    // Macro insights for corporate management
    public String getExecutiveInsights(
            String question) {

        // Collect all core HR operational metadata metrics
        long totalEmp =
            employeeRepository.count();
        long activeEmp =
            employeeRepository.countByStatus(
                EmployeeStatus.ACTIVE);
        long pendingLeaves =
            leaveRepository.countByStatus(
                LeaveStatus.PENDING);
        long approvedLeaves =
            leaveRepository.countByStatus(
                LeaveStatus.APPROVED);

        Map<String, Long> byDept = new HashMap<>();
        employeeRepository.findAll()
            .forEach(e -> {
                if (e.getDepartment() != null)
                    byDept.merge(
                        e.getDepartment(),
                        1L, Long::sum);
            });

        Map<LeaveType, Long> byLeaveType =
            new HashMap<>();
        for (LeaveType type : LeaveType.values())
            byLeaveType.put(type,
                leaveRepository.countByType(type));

        String dataContext = String.format("""
            Complete HR Datastore State:
            
            Staffing:
            - Total: %d employees
            - Active: %d
            - Activity Rate: %.1f%%
            
            Leaves:
            - Pending: %d
            - Approved: %d
            - ANNUAL: %d | SICK: %d
            - MATERNITY: %d | PATERNITY: %d
            - UNPAID: %d
            
            Distribution by Department:
            %s
            """,
            totalEmp, activeEmp,
            totalEmp > 0
                ? (double) activeEmp
                    / totalEmp * 100
                : 0,
            pendingLeaves, approvedLeaves,
            byLeaveType.getOrDefault(
                LeaveType.ANNUAL, 0L),
            byLeaveType.getOrDefault(
                LeaveType.SICK, 0L),
            byLeaveType.getOrDefault(
                LeaveType.MATERNITY, 0L),
            byLeaveType.getOrDefault(
                LeaveType.PATERNITY, 0L),
            byLeaveType.getOrDefault(
                LeaveType.UNPAID, 0L),
            byDept.entrySet().stream()
                .map(e -> "- " + e.getKey()
                    + " : " + e.getValue())
                .collect(Collectors.joining("\n")));

        String systemPrompt = """
            You are the corporate management's Shadow AI.
            You have access to all real-time HR data points.
            
            Answer strategic questions from executive management using:
            - Precise statistical figures
            - Actionable analyses
            - Concrete recommendations
            - An executive tone (concise, direct)
            - Maximum 250 words
            
            Available Data:
            """ + dataContext;

        log.info("Shadow AI: executive insight "
            + "generated for question: {}",
            question.substring(0,
                Math.min(50, question.length())));

        return geminiService.chat(
            systemPrompt, question);
    }

    // Automated weekly status update pipeline reporting
    public String generateWeeklyReport() {
        String systemPrompt = """
            Generate an executive weekly HR report
            in English.
            
            Structure:
            ## 📋 Weekly HR Report
            ### Weekly KPIs
            ### Attention Points
            ### Priority Actions
            ### Macro Trends
            
            Max 200 words. Professional tone.
            """;

        String dataContext = "Weekly data: "
            + getExecutiveInsights(
                "Weekly summary");

        return geminiService.chat(
            systemPrompt, dataContext);
    }

    // Inner class structure for parsing risk scoring components
    private static class DepartmentRisk {
        String department;
        int totalEmployees = 0;
        int highRiskEmployees = 0;
        int burnoutRisk = 0;
        int newEmployees = 0;
        int onLeaveCount = 0;

        DepartmentRisk(String department) {
            this.department = department;
        }
    }
}
