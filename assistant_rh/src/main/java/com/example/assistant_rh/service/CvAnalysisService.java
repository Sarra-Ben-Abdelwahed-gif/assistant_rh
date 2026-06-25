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
            Candidat : %s (%s)
            Lettre de motivation : %s

            Contenu du CV :
            %s
            """,
            application.getCandidateName(),
            application.getCandidateEmail(),
            application.getCoverLetter() != null
                ? application.getCoverLetter()
                : "Non fournie",
            cvContent);

        
        String aiResponse = geminiService.chat(
            systemPrompt, userPrompt);

        CvAnalysisResponse result =
            parseResponse(aiResponse);
        result.setFullAnalysis(aiResponse);

        application.setAiAnalysis(aiResponse);
        application.setScore(result.getScore());
        applicationRepository.save(application);

        log.info("Analyse CV Gemini : id={}, score={}",
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
            "CV à analyser :\n" + cvText;

        
        String aiResponse = geminiService.chat(
            systemPrompt, userPrompt);
        CvAnalysisResponse result =
            parseResponse(aiResponse);
        result.setFullAnalysis(aiResponse);
        return result;
    }

    private String extractCvText(String minioKey) {
        if (minioKey == null)
            return "CV non disponible";
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
            log.warn("Impossible de lire CV : {}",
                e.getMessage());
            return "Contenu CV non lisible";
        }
    }

    private String buildAnalysisPrompt(
            JobOffer offer) {
        return String.format("""
            Tu es un expert RH avec 15 ans d'expérience.
            Analyse ce CV pour le poste suivant :

            - Titre       : %s
            - Département : %s
            - Contrat     : %s
            - Expérience  : %s
            - Description : %s

            Réponds UNIQUEMENT en JSON valide :
            {
              "score": [0-100],
              "summary": "[résumé du profil]",
              "strengths": "[points forts]",
              "weaknesses": "[points faibles]",
              "recommendation": "[RECOMMANDE, ENTRETIEN_POSSIBLE, ou REJETE]"
            }

            Scoring :
            80-100 = Excellent
            60-79  = Bon profil
            40-59  = Profil moyen
            0-39   = Insuffisant
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
            log.warn("Parse Gemini échoué : {}",
                e.getMessage());
            result.setScore(50);
            result.setSummary("Analyse effectuée");
            result.setRecommendation(
                "ENTRETIEN_POSSIBLE");
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
