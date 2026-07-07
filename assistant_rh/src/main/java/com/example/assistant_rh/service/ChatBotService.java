package com.example.assistant_rh.service;

import com.example.assistant_rh.dto.response.ChatResponse;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.enums.Role;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import com.example.assistant_rh.repository.UserRepository;
import com.example.assistant_rh.service.mcp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotService {

    private final GeminiService geminiService;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRepository;
    private final UserRepository userRepository;

    // MCP Services
    private final McpToolRegistry mcpToolRegistry;
    private final McpIntentDetector mcpIntentDetector;
    private final McpParameterExtractor paramExtractor;
    private final McpCalendarService calendarService;
    private final McpBulkActionService bulkService;
    private final McpShadowAiService shadowAiService;
    private final McpProactiveAlertService alertService;
    private final McpConversationMemory memory;
    private final McpMatchingService matchingService;
    private final McpEmailDraftService emailDraftService;

    public ChatResponse respond(
        String message,
        List<Map<String, String>> history) {

    String email = getCurrentEmail();
    Role role = getUserRole(email);

    // Contextual memory
    try {
        memory.autoExtract(email, message);
    } catch (Exception e) {
        log.warn("Memory error : {}",
            e.getMessage());
    }

    // Detect MCP intent
    McpIntent intent =
        mcpIntentDetector.detect(message);
    String toolResult = null;

    // Global MCP try-catch
    try {
        if (intent != McpIntent.NONE) {
            toolResult = executeMcpTool(
                intent, message, email, role);
        }
    } catch (Exception e) {
        log.error("MCP tool error [{}] : {}",
            intent, e.getMessage());
        toolResult = null;
    }

    // User context
    String userContext = "";
    try {
        userContext = buildContext(email, role);
    } catch (Exception e) {
        log.warn("Context error : {}",
            e.getMessage());
        userContext = "User : " + email;
    }

    // Memory recall
    String userMemory = "";
    try {
        userMemory =
            memory.getAllMemories(email);
    } catch (Exception e) {
        log.warn("Memory recall error : {}",
            e.getMessage());
    }

    // Proactive alerts try-catch
    String proactiveAlerts = "";
    try {
        proactiveAlerts =
            getRelevantAlerts(role);
    } catch (Exception e) {
        log.warn("Alerts error : {}",
            e.getMessage());
    }

    // Build system prompt
    String systemPrompt = buildSystemPrompt(
        userContext, toolResult,
        userMemory, proactiveAlerts);

    // Gemini call try-catch
    String reply;
    try {
        reply = geminiService.chatWithHistory(
            systemPrompt, history, message);
    } catch (Exception e) {
        log.error("Gemini error : {}",
            e.getMessage());
        reply = "I am temporarily unavailable. "
            + "Please try again in a few moments.";
    }

    // Save to memory
    try {
        if (toolResult != null)
            memory.remember(email,
                "last_action", intent.name());
    } catch (Exception e) {
        log.warn("Memory save error : {}",
            e.getMessage());
    }

    return new ChatResponse(reply, "assistant");
}

    public ChatResponse respondSimple(String message) {
        return respond(message, List.of());
    }

    // ─── MCP Tool Execution Layer ─────────────────
    private String executeMcpTool(
            McpIntent intent,
            String message,
            String email,
            Role role) {
        try {
            switch (intent) {

                // ── Employee Context ─────────────────
                case GET_LEAVE_BALANCE:
                    if (role == Role.EMPLOYEE)
                        return mcpToolRegistry.getLeaveBalance(email);
                    break;

                case CREATE_LEAVE:
                    if (role == Role.EMPLOYEE)
                        return handleCreateLeave(email, message);
                    break;

                case GET_MY_LEAVES:
                    if (role == Role.EMPLOYEE)
                        return mcpToolRegistry.getMyLeaves(email);
                    break;

                case GET_MY_DOCUMENTS:
                    if (role == Role.EMPLOYEE)
                        return mcpToolRegistry.getMyDocuments(email);
                    break;

                case GET_MY_PROFILE:
                    if (role == Role.EMPLOYEE)
                        return mcpToolRegistry.getMyProfile(email);
                    break;

                case GET_TEAM_CALENDAR:
                    if (role == Role.EMPLOYEE)
                        return mcpToolRegistry.getTeamCalendar(email, 30);
                    break;

                // ── Candidate Context ───────────────
                case GET_ACTIVE_JOBS:
                    return mcpToolRegistry.getActiveJobs();

                case GET_MY_CANDIDATURES:
                    if (role == Role.CANDIDATE)
                        return mcpToolRegistry.getMyCandidatures(email);
                    break;

                // ── Admin Context ────────────────────
                case GET_HR_STATS:
                    if (role == Role.HR_ADMIN)
                        return mcpToolRegistry.getHrStats();
                    break;

                case SEARCH_EMPLOYEE:
                    if (role == Role.HR_ADMIN) {
                        String query = paramExtractor.extractSearchQuery(message);
                        return mcpToolRegistry.searchEmployee(query);
                    }
                    break;

                case GET_PENDING_LEAVES:
                    if (role == Role.HR_ADMIN)
                        return mcpToolRegistry.getPendingLeaves();
                    break;

                case PREVIEW_APPROVALS:
                    if (role == Role.HR_ADMIN)
                        return bulkService.previewPendingApprovals();
                    break;

                case APPROVE_ALL_LEAVES:
                    if (role == Role.HR_ADMIN)
                        return bulkService.approveAllSafe(
                            email, "Approved via AI Virtual Assistant");
                    break;

                case GET_PROACTIVE_ALERTS:
                    if (role == Role.HR_ADMIN)
                        return alertService.getAlertsSummary();
                    break;

                case GET_TURNOVER_RISK:
                    if (role == Role.HR_ADMIN)
                        return shadowAiService.analyzeTurnoverRisk();
                    break;

                case EXECUTIVE_QUESTION:
                    if (role == Role.HR_ADMIN)
                        return shadowAiService.getExecutiveInsights(message);
                    break;

                default:
                    break;
            }
            return "Action unauthorized for your security access level.";
        } catch (Exception e) {
            log.error("MCP Execution Error {} : {}", intent, e.getMessage());
            return "An processing error occurred while executing the action: " + e.getMessage();
        }
    }

    // ─── Intelligent Leave Request Management ─────
    private String handleCreateLeave(String email, String message) {
        var params = paramExtractor.extractLeaveParams(message);

        if (!params.containsKey("startDate") || !params.containsKey("endDate"))
            return "To submit a leave request, please explicitly state the dates.\n"
                + "Example: 'Je veux poser un congé du 01/07/2026 au 15/07/2026'";

        // Pre-evaluating scheduling overlap conflicts before submitting
        try {
            LocalDate start = LocalDate.parse(params.get("startDate"));
            LocalDate end = LocalDate.parse(params.get("endDate"));

            McpCalendarService.ConflictAnalysis analysis = 
                calendarService.analyzeConflicts(email, start, end);

            if (analysis.hasConflict()) {
                // Find and extract structural alternative windows
                long duration = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                String alternatives = calendarService.suggestAlternativeDates(email, start, (int) duration);

                StringBuilder warning = new StringBuilder();
                warning.append("⚠️ Schedule conflict detected within your team members!\n\n");

                analysis.conflicts().forEach(c ->
                    warning.append(String.format(
                        "• %s (%s) is absent from %s to %s\n",
                        c.employeeName(), c.position(), c.startDate(), c.endDate())));

                warning.append(String.format(
                    "\n📊 Current team absence threshold rate: %d%%\n\n",
                    analysis.absenceRatePercent()));
                warning.append(alternatives);
                warning.append("\n\n💬 How would you like to proceed?:\n"
                    + "1️⃣ Maintain current dates and submit anyway\n"
                    + "2️⃣ Apply the suggested alternative scheduling window");

                // Save transient context parameters to fallback storage
                memory.remember(email, "pending_leave_start", params.get("startDate"));
                memory.remember(email, "pending_leave_end", params.get("endDate"));
                memory.remember(email, "pending_leave_type", params.getOrDefault("type", "ANNUAL"));

                return warning.toString();
            }

        } catch (Exception e) {
            log.warn("Calendar context overlap parsing exception: {}", e.getMessage());
        }

        // Safe execution path - Proceed directly if no resource conflicts are raised
        return mcpToolRegistry.createLeaveRequest(
            email,
            params.get("startDate"),
            params.get("endDate"),
            params.getOrDefault("type", "ANNUAL"),
            params.get("reason"));
    }

    // ─── Fetch Relevant Live Security Alerts ──────
    private String getRelevantAlerts(Role role) {
        if (role != Role.HR_ADMIN)
            return "";
        var alerts = alertService.getAlerts();
        if (alerts.isEmpty()) return "";
        return "\n\n🔔 Active Alerts (" + alerts.size() + ") : " + alertService.getAlertsSummary();
    }

    // ─── Construct Identity Context Maps ──────────
    private String buildContext(String email, Role role) {
        if (role == Role.HR_ADMIN)
            return "HR Administrator: " + email;
        if (role == Role.CANDIDATE)
            return "Candidate: " + email;

        Optional<Employee> optEmp = employeeRepository.findByEmail(email);
        if (optEmp.isEmpty())
            return "User: " + email;

        Employee emp = optEmp.get();
        List<LeaveRequest> leaves = leaveRepository.findByEmployeeId(emp.getId());
        long pending = leaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        long approved = leaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();

        return String.format("""
            Employee: %s %s | %s | %s
            Available Leave Balance: %d days
            Pending Requests: %d
            Approved Requests: %d
            """,
            emp.getFirstName(), emp.getLastName(),
            emp.getDepartment(), emp.getPosition(),
            emp.getAnnualLeaveBalance(), pending, approved);
    }

    // ─── Build Complete System Prompt ─────────────
    private String buildSystemPrompt(
            String userContext,
            String toolResult,
            String userMemory,
            String proactiveAlerts) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            You are an Enterprise-grade HR Virtual Assistant — intelligent, proactive,
            and capable of taking direct action via tools.

            Your Superpowers:
            ✅ View, check balances, and request leaves
            ✅ Analyze team calendar overlaps and scheduling conflicts
            ✅ Bulk approve pending requests (Admin only)
            ✅ Generate strategic HR and talent management reports
            ✅ Analyze turnover risks and employee retention metrics
            ✅ Draft professional corporate emails
            ✅ Deliver intelligent proactive alerts
            ✅ Leverage conversation memory for context

            Rules:
            - Language: Respond naturally in the SAME language used by the user (English or French).
            - Tone: Maintain a professional, empathetic, and constructive tone.
            - Action Confirmation: If you execute an action or tool, always confirm it clearly.
            - Conflict Management: If a team scheduling conflict is detected, propose alternative dates.
            - Length: Keep responses under 4 paragraphs unless generating an executive report.

            User Context:
            """);
        prompt.append(userContext);

        if (userMemory != null && !userMemory.isBlank()) {
            prompt.append("\n\nContext Memory: \n");
            prompt.append(userMemory);
        }

        if (toolResult != null && !toolResult.isBlank()) {
            prompt.append("""

                ════════════════════════
                REAL-TIME DATA (GROUND TRUTH):
                ════════════════════════
                """);
            prompt.append(toolResult);
            prompt.append("""

                ════════════════════════
                Use these exact data points to construct your answer. Do not hallucinate.
                """);
        }

        if (proactiveAlerts != null && !proactiveAlerts.isBlank()) {
            prompt.append(proactiveAlerts);
        }

        return prompt.toString();
    }

    private Role getUserRole(String email) {
        return userRepository.findByEmail(email)
            .map(u -> u.getRole())
            .orElse(Role.CANDIDATE);
    }

    private String getCurrentEmail() {
        return SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();
    }
}