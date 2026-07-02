package com.example.assistant_rh.controller;

import com.example.assistant_rh.dto.request.BulkApproveRequest;
import com.example.assistant_rh.dto.request.InterviewEvaluationRequest;
import com.example.assistant_rh.dto.response.McpAlertDTO;
import com.example.assistant_rh.service.mcp.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpProactiveAlertService alertService;
    private final McpBulkActionService bulkService;
    private final McpShadowAiService shadowAiService;
    private final McpMatchingService matchingService;
    private final McpEmailDraftService emailDraftService;
    private final McpCalendarService calendarService;
    private final McpOnboardingService onboardingService;
    private final McpConversationMemory conversationMemory;

    // ── Proactive Alerts ──────────────────────
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<List<McpAlertDTO>> getAlerts() {
        List<McpAlertDTO> dtos = alertService
            .getAlerts()
            .stream()
            .map(a -> new McpAlertDTO(
                a.type(),
                a.message(),
                a.code(),
                a.date(),
                null))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ── Bulk Approvals Preview ────────────────
    @GetMapping("/bulk/preview")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> previewBulkApproval() {
        return ResponseEntity.ok(
            bulkService.previewPendingApprovals());
    }

    // ── Bulk Approval Action ──────────────────
    @PostMapping("/bulk/approve")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> bulkApprove(
            @RequestBody BulkApproveRequest req) {
        String adminEmail = getCurrentEmail();
        if (!req.isConfirmed())
            return ResponseEntity.ok(
                "Please confirm by setting confirmed=true.");
        return ResponseEntity.ok(
            bulkService.approveAllSafe(
                adminEmail,
                req.getAdminComment()));
    }

    // ── Turnover Risk Report ──────────────────
    @GetMapping("/shadow/turnover")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> getTurnoverRisk() {
        return ResponseEntity.ok(
            shadowAiService.analyzeTurnoverRisk());
    }

    // ── Executive Insight Query ───────────────
    @PostMapping("/shadow/insight")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> getInsight(
            @RequestBody java.util.Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.isBlank())
            return ResponseEntity.badRequest()
                .body("Question parameter is required.");
        return ResponseEntity.ok(
            shadowAiService.getExecutiveInsights(question));
    }

    // ── Weekly HR Report ──────────────────────
    @GetMapping("/shadow/weekly-report")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> getWeeklyReport() {
        return ResponseEntity.ok(
            shadowAiService.generateWeeklyReport());
    }

    // ── Top Candidates Matching ───────────────
    @GetMapping("/matching/{jobOfferId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> getTopCandidates(
            @PathVariable Long jobOfferId) {
        return ResponseEntity.ok(
            matchingService.getTopCandidates(jobOfferId));
    }

    // ── Post-Interview Evaluation Sheet ───────
    @PostMapping("/matching/evaluation")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> evaluate(
            @Valid @RequestBody InterviewEvaluationRequest req) {
        return ResponseEntity.ok(
            matchingService.generateEvaluationSheet(
                req.getApplicationId(),
                req.getInterviewNotes()));
    }

    // ── Job Offer / Hire Email ────────────────
    @PostMapping("/email/hire/{appId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> sendHireEmail(
            @PathVariable Long appId) {
        return ResponseEntity.ok(
            emailDraftService.sendHireEmail(appId));
    }

    // ── Application Rejection Email ───────────
    @PostMapping("/email/reject/{appId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> sendRejectionEmail(
            @PathVariable Long appId,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(
            emailDraftService.sendRejectionEmail(
                appId,
                body.get("feedback")));
    }

    // ── Interview Invitation Email ─────────────
    @PostMapping("/email/interview/{appId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> sendInterviewInvite(
            @PathVariable Long appId,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(
            emailDraftService.sendInterviewInvite(
                appId,
                body.get("dateTime"),
                body.get("location")));
    }

    // ── Department Calendar View ──────────────
    @GetMapping("/calendar/{dept}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> getDeptCalendar(
            @PathVariable String dept,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(
            calendarService.getDepartmentCalendar(dept, days));
    }

    // ── Generate Onboarding Plan ──────────────
    @PostMapping("/onboarding/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> generateOnboarding(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
            onboardingService.generateOnboardingPlan(employeeId));
    }

    // ── Active Onboarding Pipeline Status ─────
    @GetMapping("/onboarding/status")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> getOnboardingStatus() {
        return ResponseEntity.ok(
            onboardingService.getOnboardingStatus());
    }

    // ── Flush Conversation Memory Cache ───────
    @DeleteMapping("/memory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> clearMemory() {
        conversationMemory.clear(getCurrentEmail());
        return ResponseEntity.ok("Context memory cleared successfully.");
    }

    private String getCurrentEmail() {
        return SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();
    }
}
