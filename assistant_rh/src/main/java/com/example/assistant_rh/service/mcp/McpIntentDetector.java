package com.example.assistant_rh.service.mcp;

import org.springframework.stereotype.Component;

@Component
public class McpIntentDetector {

    public McpIntent detect(String message) {
        String msg = message.toLowerCase().trim();

        // ── Leave Balance ──────────────────────────
        if (containsAny(msg,
                "balance", "how many days",
                "remaining days", "remaining leave",
                "available days"))
            return McpIntent.GET_LEAVE_BALANCE;

        // ── Create Leave Request ───────────────────────────
        if (containsAny(msg,
                "create a request", "request leave",
                "apply for leave", "take a leave",
                "ask for time off", "i want to take a leave"))
            return McpIntent.CREATE_LEAVE;

        // ── My Leaves ────────────────────────────
        if (containsAny(msg,
                "my leaves", "my requests",
                "leave history", "list my leaves", "my vacations"))
            return McpIntent.GET_MY_LEAVES;

        // ── My documents ─────────────────────────
        if (containsAny(msg,
                "my documents", "my files",
                "my contract", "my payslip",
                "certificate","pay stub"))
            return McpIntent.GET_MY_DOCUMENTS;

        // ── My Profile ────────────────────────────
        if (containsAny(msg,
                "my profile", "my information","my info",
                "my position", "my department","my job",
                "seniority" , "tenure"))
            return McpIntent.GET_MY_PROFILE;

        // ── Job Offers ─────────────────────────
        if (containsAny(msg,
                "job offers", "available jobs" , "openings",
                "open positions", "recruitment",
                "careers", "job opening"))
            return McpIntent.GET_ACTIVE_JOBS;

        // ── Mes candidatures ──────────────────────
        if (containsAny(msg,
                "my applications", "i have applied",
                "my application", "application status"))
            return McpIntent.GET_MY_CANDIDATURES;

        // ── HR Stats  (admin) ──────────────────────
        if (containsAny(msg,
                "statistics", "hr stats",
                "dashboard", "hr summary",
                "report"))
            return McpIntent.GET_HR_STATS;

        // ── Employee Search (admin) ──────────────
        if (containsAny(msg,
                "search", "find ", "research",
                "who is", "find employee"))
            return McpIntent.SEARCH_EMPLOYEE;

        // ── Congés en attente (admin) ─────────────
        if (containsAny(msg,
                "pending", "to process", "pending requests",
                "leaves to validate", "awaiting approval"))
            return McpIntent.GET_PENDING_LEAVES;

        // ── Team Calendar ─────────────────────────────
        if (containsAny(msg,
                "team calendar", "who is absent",
                "team absences", "team schedule",
                "department calendar"))
            return McpIntent.GET_TEAM_CALENDAR;

        // ── Proactive Alerts ──────────────────────────
        if (containsAny(msg,
                "alerts", "notifications",
                "are there any issues",
                "what's new", "urgent summary"))
            return McpIntent.GET_PROACTIVE_ALERTS;

        // ── Turnover Risk ─────────────────────────────
        if (containsAny(msg,
                "turnover", "departure risk",
                "who is at risk of leaving",
                "social climate", "retention",
                "hr analysis"))
            return McpIntent.GET_TURNOVER_RISK;

        // ── Bulk Approval ─────────────────────────────
        if (containsAny(msg,
                "approve all", "validate all",
                "approve all leaves",
                "validate all requests"))
            return McpIntent.APPROVE_ALL_LEAVES;

        // ── Approvals Preview ─────────────────────────
        if (containsAny(msg,
                "view requests",
                "list pending leaves",
                "show requests"))
            return McpIntent.PREVIEW_APPROVALS;

        // ── Executive Question ─────────────────────────
        if (containsAny(msg,
                "report", "analysis",
                "hr performance",
                "which department", "hr status",
                "summary", "insights", "recommendation"))
            return McpIntent.EXECUTIVE_QUESTION;

        return McpIntent.NONE;
    }

    private boolean containsAny(
            String msg, String... keywords) {
        for (String kw : keywords)
            if (msg.contains(kw)) return true;
        return false;
    }
}
