package com.example.assistant_rh.service.mcp;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class McpParameterExtractor {

    private static final DateTimeFormatter[] FORMATS = {
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.FRENCH) // Keep French just in case
    };

    // Extract leave parameters from the message
    public Map<String, String> extractLeaveParams(String message) {
        Map<String, String> params = new HashMap<>();
        String msg = message.toLowerCase();

        // Extract dates in dd/MM/yyyy or yyyy-MM-dd format
        Pattern datePattern = Pattern.compile(
            "\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}"
            + "|\\d{4}-\\d{2}-\\d{2})\\b");
        Matcher matcher = datePattern.matcher(message);

        String[] dates = new String[2];
        int count = 0;
        while (matcher.find() && count < 2) {
            dates[count++] = matcher.group();
        }

        if (count >= 1)
            params.put("startDate", normalizeDate(dates[0]));
        if (count >= 2)
            params.put("endDate", normalizeDate(dates[1]));

        // Extract leave type
        if (msg.contains("sick") || msg.contains("maladie"))
            params.put("type", "SICK");
        else if (msg.contains("maternity") || msg.contains("maternité"))
            params.put("type", "MATERNITY");
        else if (msg.contains("paternity") || msg.contains("paternité"))
            params.put("type", "PATERNITY");
        else if (msg.contains("unpaid") || msg.contains("sans solde"))
            params.put("type", "UNPAID");
        else
            params.put("type", "ANNUAL");

        // Extract reason (matches "for", "reason", "motif", etc.)
        Pattern reasonPattern = Pattern.compile(
            "(?:for|reason|motif|raison)[:\\s]+([^,\\.]+)",
            Pattern.CASE_INSENSITIVE);
        Matcher reasonMatcher = reasonPattern.matcher(message);
        if (reasonMatcher.find())
            params.put("reason", reasonMatcher.group(1).trim());

        return params;
    }

    // Extract search query
    public String extractSearchQuery(String message) {
        String msg = message.toLowerCase();
        String[] triggers = {
            "search ", "find ", "look up ", "research ",
            "who is ", "find employee ", "search employee "
        };
        for (String t : triggers) {
            int idx = msg.indexOf(t);
            if (idx != -1) {
                return message.substring(idx + t.length()).trim();
            }
        }
        return message;
    }

    private String normalizeDate(String dateStr) {
        if (dateStr == null) return null;
        // Convert dd/MM/yyyy → yyyy-MM-dd
        if (dateStr.contains("/")) {
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                return parts[2] + "-"
                    + String.format("%02d", Integer.parseInt(parts[1]))
                    + "-"
                    + String.format("%02d", Integer.parseInt(parts[0]));
            }
        }
        return dateStr;
    }
}
