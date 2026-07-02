package com.example.assistant_rh.service.mcp;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class McpConversationMemory {

    // User-specific memory map
    // key = email, value = list of memory entries
    private final Map<String,
        List<MemoryEntry>> memory =
            new ConcurrentHashMap<>();

    // Maximum memory slots allocated per user
    private static final int MAX_MEMORIES = 20;

    // Save a specific memory entry
    public void remember(String email,
            String key, String value) {
        memory.computeIfAbsent(email,
            k -> new ArrayList<>());

        List<MemoryEntry> userMemory =
            memory.get(email);

        // Evict older entry if the
        // same key already exists
        userMemory.removeIf(
            m -> m.key().equals(key));

        userMemory.add(new MemoryEntry(
            key, value, LocalDateTime.now()));

        // Keep list size within bounds
        if (userMemory.size() > MAX_MEMORIES)
            userMemory.remove(0);
    }

    // Recall a specific memory entry
    public String recall(String email,
            String key) {
        List<MemoryEntry> userMemory =
            memory.getOrDefault(
                email, List.of());
        return userMemory.stream()
            .filter(m -> m.key().equals(key))
            .map(MemoryEntry::value)
            .findFirst()
            .orElse(null);
    }

    // Retrieve all recorded memories 
    // associated with a user account
    public String getAllMemories(String email) {
        List<MemoryEntry> userMemory =
            memory.getOrDefault(
                email, List.of());
        if (userMemory.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder(
            "What I remember about you:\n");
        userMemory.forEach(m ->
            sb.append("- ").append(m.key())
                .append(": ").append(m.value())
                .append("\n"));
        return sb.toString();
    }

    // Automatically parse and capture relevant
    // contextual indicators inside a message block
    public void autoExtract(
            String email, String message) {
        String msg = message.toLowerCase();

        // Identify leave request preference indicators
        if (msg.contains("prefer")
                || msg.contains("i like")
                || msg.contains("i love")) {
            remember(email,
                "leave_preference", message);
        }

        // Identify specific active project track mentions
        if (msg.contains("project")
                || msg.contains("deadline")) {
            remember(email,
                "project_context", message);
        }

        // Identify upcoming prospective leave timeframe indicators
        if (msg.contains("planning to")
                || msg.contains("intend to")
                || msg.contains("scheduled to")) {
            remember(email,
                "leave_intent", message);
        }
    }

    // Clear a given user's memory map cache completely
    public void clear(String email) {
        memory.remove(email);
    }

    public record MemoryEntry(
        String key,
        String value,
        LocalDateTime timestamp) {}
}
