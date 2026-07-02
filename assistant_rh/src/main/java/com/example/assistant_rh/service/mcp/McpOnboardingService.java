package com.example.assistant_rh.service.mcp;

import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.service.EmailService;
import com.example.assistant_rh.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpOnboardingService {

    private final EmployeeRepository
        employeeRepository;
    private final EmailService emailService;
    private final GeminiService geminiService;

    // Generate comprehensive onboarding plan
    public String generateOnboardingPlan(
            Long employeeId) {
        Employee emp = employeeRepository
            .findById(employeeId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Employee not found"));

        String systemPrompt = String.format("""
            You are an expert HR onboarding analyst.
            Generate a comprehensive, tailored, and
            personalized onboarding plan for this new hire.
            
            Employee: %s %s
            Role: %s
            Department: %s
            Hire Date: %s
            
            Plan Format:
            
            ## 🎯 Onboarding Plan — %s %s
            
            ### Day 1 — Welcome & Orientation
            - [ ] Task 1
            - [ ] Task 2
            
            ### Week 1 — Setup & Integration
            - [ ] Task 1
            
            ### Month 1 — Upskilling & Core Training
            - [ ] Objective 1
            
            ### Month 3 — Milestone Evaluation
            - [ ] Evaluation review checkpoint
            
            ### Key Contacts to Meet
            - [list of contacts]
            
            ### Tools to Master
            - [list of platforms/tools]
            
            ### 90-Day Success KPIs
            - [success metrics]
            
            Be precise, professional, practical, and highly motivating.
            """,
            emp.getFirstName(),
            emp.getLastName(),
            emp.getPosition(),
            emp.getDepartment(),
            emp.getHireDate(),
            emp.getFirstName(),
            emp.getLastName());

        String plan = geminiService.chat(
            systemPrompt,
            "Generate the onboarding plan now.");

        // Deliver the structured plan via email pipeline
        try {
            emailService.sendWelcomeEmail(
                emp.getEmail(),
                emp.getFirstName()
                    + " " + emp.getLastName(),
                "Your onboarding plan "
                    + "is now available");
        } catch (Exception e) {
            log.warn("Onboarding email notification failed: {}",
                e.getMessage());
        }

        log.info("Onboarding plan successfully generated for "
            + "employee id={}", employeeId);
        return plan;
    }

    // Monitor recent hires currently undergoing active onboarding tracks
    public String getOnboardingStatus() {
        LocalDate threeMonthsAgo =
            LocalDate.now().minusMonths(3);

        List<Employee> newEmployees =
            employeeRepository.findAll()
                .stream()
                .filter(e ->
                    e.getHireDate() != null
                    && e.getHireDate()
                        .isAfter(threeMonthsAgo))
                .toList();

        if (newEmployees.isEmpty())
            return "No new hires are currently in "
                + "the onboarding window.";

        StringBuilder sb = new StringBuilder(
            String.format(
                "👋 %d employee(s) currently in "
                + "onboarding phase:\n\n",
                newEmployees.size()));

        newEmployees.forEach(e -> {
            long days = ChronoUnit.DAYS.between(
                e.getHireDate(), LocalDate.now());
            String phase = days <= 7
                ? "🟡 Week 1"
                : days <= 30
                ? "🟠 Month 1"
                : "🟢 Months 2-3";

            sb.append(String.format(
                "• %s %s | %s | %s | "
                + "Active for %d days\n",
                e.getFirstName(),
                e.getLastName(),
                e.getPosition(),
                phase, days));
        });

        return sb.toString();
    }
}
