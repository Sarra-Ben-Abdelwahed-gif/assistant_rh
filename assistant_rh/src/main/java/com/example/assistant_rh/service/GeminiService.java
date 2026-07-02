package com.example.assistant_rh.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper =
        new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    
    public String chat(String systemPrompt,
            String userMessage) {
        try {
            String fullMessage = systemPrompt
                + "\n\n" + userMessage;

            String requestBody =
                buildSimpleRequest(fullMessage);

            return callGemini(requestBody);

        } catch (Exception e) {
            log.error("Error Gemini chat : {}",
                e.getMessage());
            throw new RuntimeException(
                "AI service temporarily unavailable");
        }
    }

    
    public String chatWithHistory(
            String systemPrompt,
            List<Map<String, String>> history,
            String newMessage) {
        try {
            String requestBody = buildHistoryRequest(
                systemPrompt, history, newMessage);

            return callGemini(requestBody);

        } catch (Exception e) {
            log.error("Error Gemini history : {}",
                e.getMessage());
            throw new RuntimeException(
                "AI service temporarily unavailable");
        }
    }

    
    private String buildSimpleRequest(String text)
            throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode contents =
            objectMapper.createArrayNode();
        ObjectNode content =
            objectMapper.createObjectNode();
        content.put("role", "user");

        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", text);
        parts.add(part);
        content.set("parts", parts);
        contents.add(content);
        root.set("contents", contents);

        
        ObjectNode genConfig =
            objectMapper.createObjectNode();
        genConfig.put("temperature", 0.7);
        genConfig.put("maxOutputTokens", 1000);
        root.set("generationConfig", genConfig);

        return objectMapper.writeValueAsString(root);
    }

    
    private String buildHistoryRequest(
            String systemPrompt,
            List<Map<String, String>> history,
            String newMessage) throws Exception {

        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents =
            objectMapper.createArrayNode();

        
        if (systemPrompt != null
                && !systemPrompt.isBlank()) {
            ObjectNode sysContent =
                objectMapper.createObjectNode();
            sysContent.put("role", "user");
            ArrayNode sysParts =
                objectMapper.createArrayNode();
            ObjectNode sysPart =
                objectMapper.createObjectNode();
            sysPart.put("text", systemPrompt);
            sysParts.add(sysPart);
            sysContent.set("parts", sysParts);
            contents.add(sysContent);

            
            ObjectNode sysReply =
                objectMapper.createObjectNode();
            sysReply.put("role", "model");
            ArrayNode replyParts =
                objectMapper.createArrayNode();
            ObjectNode replyPart =
                objectMapper.createObjectNode();
            replyPart.put("text",
                "Understood. Ready to help.");
            replyParts.add(replyPart);
            sysReply.set("parts", replyParts);
            contents.add(sysReply);
        }

        
        if (history != null) {
            for (Map<String, String> msg : history) {
                String role = "user".equals(
                    msg.get("role")) ? "user" : "model";
                ObjectNode msgNode =
                    objectMapper.createObjectNode();
                msgNode.put("role", role);
                ArrayNode msgParts =
                    objectMapper.createArrayNode();
                ObjectNode msgPart =
                    objectMapper.createObjectNode();
                msgPart.put("text",
                    msg.get("content"));
                msgParts.add(msgPart);
                msgNode.set("parts", msgParts);
                contents.add(msgNode);
            }
        }

        
        ObjectNode newContent =
            objectMapper.createObjectNode();
        newContent.put("role", "user");
        ArrayNode newParts =
            objectMapper.createArrayNode();
        ObjectNode newPart =
            objectMapper.createObjectNode();
        newPart.put("text", newMessage);
        newParts.add(newPart);
        newContent.set("parts", newParts);
        contents.add(newContent);

        root.set("contents", contents);

        ObjectNode genConfig =
            objectMapper.createObjectNode();
        genConfig.put("temperature", 0.7);
        genConfig.put("maxOutputTokens", 1000);
        root.set("generationConfig", genConfig);

        return objectMapper.writeValueAsString(root);
    }

    
    private String callGemini(String requestBodyJson)
            throws Exception {
        String url = apiUrl + "?key=" + apiKey;

        RequestBody body = RequestBody.create(
            requestBodyJson,
            MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response =
                okHttpClient.newCall(request)
                    .execute()) {

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null
                    ? response.body().string() : "null";
                log.error("Gemini error {} : {}",
                    response.code(), errorBody);
                throw new RuntimeException(
                    "Error Gemini : "
                    + response.code());
            }

            String responseBody =
                response.body().string();
            return extractContent(responseBody);
        }
    }

    
    private String extractContent(String responseJson)
            throws Exception {
        JsonNode root = objectMapper
            .readTree(responseJson);
        return root
            .path("candidates")
            .get(0)
            .path("content")
            .path("parts")
            .get(0)
            .path("text")
            .asText();
    }
}
