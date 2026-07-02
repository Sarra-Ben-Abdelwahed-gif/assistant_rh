package com.example.assistant_rh.service.mcp;

public enum McpIntent {
    // ── Employee ───────────────────────────────
    GET_LEAVE_BALANCE,
    CREATE_LEAVE,
    GET_MY_LEAVES,
    GET_MY_DOCUMENTS,
    GET_MY_PROFILE,
    GET_TEAM_CALENDAR,

    // ── Candidate ──────────────────────────────
    GET_ACTIVE_JOBS,
    GET_MY_CANDIDATURES,

    // ── Admin ─────────────────────────────────
    GET_HR_STATS,
    SEARCH_EMPLOYEE,
    GET_PENDING_LEAVES,
    APPROVE_ALL_LEAVES,
    PREVIEW_APPROVALS,
    GET_PROACTIVE_ALERTS,
    GET_TURNOVER_RISK,
    EXECUTIVE_QUESTION,
    GET_TEAM_CALENDAR_ADMIN,
    GET_ONBOARDING_STATUS,

    NONE
}
