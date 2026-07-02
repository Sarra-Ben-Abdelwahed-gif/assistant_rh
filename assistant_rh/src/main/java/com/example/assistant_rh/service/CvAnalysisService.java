package com.example.assistant_rh.service;

import com.example.assistant_rh.dto.response.CvAnalysisResponse;
import com.example.assistant_rh.entity.Application;
import com.example.assistant_rh.entity.JobOffer;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.ApplicationRepository;
import com.example.assistant_rh.repository.JobOfferRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CvAnalysisService {

    
    private final GeminiService geminiService;
    private final ApplicationRepository
        applicationRepository;
    private final JobOfferRepository
        jobOfferRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket-documents}")
    private String bucket;

    public CvAnalysisResponse analyzeApplication(
            Long applicationId) {
        Application application =
            applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Application", "id",
                        applicationId));

        JobOffer offer = application.getJobOffer();
        String cvContent = extractCvText(
            application.getCvMinioKey());

        String systemPrompt =
            buildAnalysisPrompt(offer);
        String userPrompt = String.format("""
            Candidate : %s (%s)
            motivation letter : %s

            CV content :
            %s
            """,
            application.getCandidateName(),
            application.getCandidateEmail(),
            application.getCoverLetter() != null
                ? application.getCoverLetter()
                : "Not provided",
            cvContent);

        
        String aiResponse = geminiService.chat(
            systemPrompt, userPrompt);

        CvAnalysisResponse result =
            parseResponse(aiResponse);
        result.setFullAnalysis(aiResponse);

        application.setAiAnalysis(aiResponse);
        application.setScore(result.getScore());
        applicationRepository.save(application);

        log.info("Gemini CV analysis: id={}, score={}",
            applicationId, result.getScore());
        return result;
    }

    public CvAnalysisResponse quickAnalyze(
            String cvText, Long jobOfferId) {
        JobOffer offer = jobOfferRepository
                .findById(jobOfferId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "JobOffer", "id", jobOfferId));

        String systemPrompt =
            buildAnalysisPrompt(offer);
        String userPrompt =
            "CV to analyze :\n" + cvText;

        
        String aiResponse = geminiService.chat(
            systemPrompt, userPrompt);
        CvAnalysisResponse result =
            parseResponse(aiResponse);
        result.setFullAnalysis(aiResponse);
        return result;
    }

    private String extractCvText(String minioKey) {
        if (minioKey == null)
            return "CV not available";
        try {
            InputStream stream =
                minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(minioKey)
                        .build());
            return new String(
                stream.readAllBytes(),
                StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Unable to read CV: {}",
                e.getMessage());
            return "CV content unreadable";
        }
    }

    private String buildAnalysisPrompt(
            JobOffer offer) {
        return String.format("""
            You are an HR expert with 15 years of experience.
Analyze this CV for the following position:

- Title       : %s
- Department  : %s
- Contract    : %s
- Experience  : %s
- Description : %s

Reply ONLY in valid JSON:
{
  "score": [0-100],
  "summary": "[profile summary]",
  "strengths": "[strengths]",
  "weaknesses": "[weaknesses]",
  "recommendation": "[RECOMMENDED, INTERVIEW_POSSIBLE, or REJECTED]"
}

Scoring:
80-100 = Excellent
60-79  = Good profile
40-59  = Average profile
0-39   = Insufficient
            """,
            offer.getTitle(),
            offer.getDepartment(),
            offer.getContractType(),
            offer.getExperienceRequired(),
            offer.getDescription());
    }

    private CvAnalysisResponse parseResponse(
            String response) {
        CvAnalysisResponse result =
            new CvAnalysisResponse();
        try {
            String json = response.trim();
            if (json.contains("```json")) {
                json = json.substring(
                    json.indexOf("```json") + 7);
                json = json.substring(
                    0, json.indexOf("```"));
            } else if (json.contains("```")) {
                json = json.substring(
                    json.indexOf("```") + 3);
                json = json.substring(
                    0, json.indexOf("```"));
            }
            json = json.trim();

            result.setScore(
                extractInt(json, "score"));
            result.setSummary(
                extractString(json, "summary"));
            result.setStrengths(
                extractString(json, "strengths"));
            result.setWeaknesses(
                extractString(json, "weaknesses"));
            result.setRecommendation(
                extractString(json, "recommendation"));
        } catch (Exception e) {
            log.warn("Gemini parsing failed: {}",
                e.getMessage());
            result.setScore(50);
            result.setSummary("Analysis completed");
            result.setRecommendation(
                "INTERVIEW_POSSIBLE");
        }
        return result;
    }

    private Integer extractInt(
            String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx == -1) return 0;
            String sub = json.substring(
                idx + key.length() + 3);
            sub = sub.replaceAll("[^0-9]", " ").trim();
            return Integer.parseInt(
                sub.split("\\s+")[0]);
        } catch (Exception e) { return 0; }
    }

    private String extractString(
            String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int idx = json.indexOf(pattern);
            if (idx == -1) return "";
            String sub = json.substring(
                idx + pattern.length()).trim();
            if (sub.startsWith("\"")) {
                sub = sub.substring(1);
                return sub.substring(
                    0, sub.indexOf("\""));
            }
            return sub.split("[,\n}]")[0].trim();
        } catch (Exception e) { return ""; }
    }
}
