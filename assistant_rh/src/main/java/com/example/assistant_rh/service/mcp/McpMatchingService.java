package com.example.assistant_rh.service.mcp;

import com.example.assistant_rh.entity.Application;
import com.example.assistant_rh.entity.JobOffer;
import com.example.assistant_rh.enums.ApplicationStatus;
import com.example.assistant_rh.repository.ApplicationRepository;
import com.example.assistant_rh.repository.JobOfferRepository;
import com.example.assistant_rh.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class McpMatchingService {

    private final ApplicationRepository
        applicationRepository;
    private final JobOfferRepository
        jobOfferRepository;
    private final GeminiService geminiService;

    // Top candidates for a job offer
    public String getTopCandidates(Long jobOfferId) {
        JobOffer offer = jobOfferRepository
            .findById(jobOfferId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Job offer not found: "
                    + jobOfferId));

        List<Application> apps =
            applicationRepository
                .findByJobOfferId(jobOfferId)
                .stream()
                .filter(a ->
                    a.getStatus()
                        != ApplicationStatus.REJECTED)
                .sorted(Comparator
                    .comparingInt(a ->
                        a.getScore() != null
                            ? -a.getScore() : 0))
                .limit(5)
                .collect(Collectors.toList());

        if (apps.isEmpty())
            return "No applications received "
                + "for this job offer.";

        StringBuilder sb = new StringBuilder(
            String.format(
                "🏆 Top candidates for '%s' :\n\n",
                offer.getTitle()));

        int rank = 1;
        for (Application a : apps) {
            String scoreStr = a.getScore() != null
                ? a.getScore() + "/100"
                : "Not analyzed";
            sb.append(String.format(
                "%d. %s (%s)\n"
                + "   AI Score: %s"
                + " | Status: %s\n"
                + "   Email: %s\n\n",
                rank++,
                a.getCandidateName(),
                a.getCandidatePhone() != null
                    ? a.getCandidatePhone()
                    : "N/A",
                scoreStr,
                a.getStatus(),
                a.getCandidateEmail()));
        }

        sb.append("💬 Would you like me to generate "
            + "interview invitation emails "
            + "for the top 3 candidates?");

        return sb.toString();
    }

    // Generate evaluation sheet post-interview
    public String generateEvaluationSheet(
            Long applicationId,
            String interviewNotes) {

        Application app = applicationRepository
            .findById(applicationId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Application not found"));

        JobOffer offer = app.getJobOffer();

        String systemPrompt = String.format("""
            You are an HR expert.
            Generate a structured and professional
            evaluation sheet for this candidate.

            Position: %s
            Candidate: %s
            Initial Resume Score: %s/100
            Resume Analysis: %s

            Output Format:
            ## Post-Interview Evaluation Sheet

            **Candidate:** [name]
            **Position:** [position]
            **Date:** [today]

            ### Interview Summary
            [summary in 2-3 sentences]

            ### Confirmed Strengths
            - [point 1]
            - [point 2]

            ### Areas for Improvement
            - [point 1]

            ### Final Score
            Resume: X/100 | Interview: X/100
            Overall Score: X/100

            ### Final Recommendation
            [RECOMMENDED / POTENTIAL_INTERVIEW
             / REJECTED] with justification

            ### Suggested Next Step
            [concrete action]
            """,
            offer != null
                ? offer.getTitle() : "N/A",
            app.getCandidateName(),
            app.getScore() != null
                ? app.getScore() : "N/A",
            app.getAiAnalysis() != null
                ? app.getAiAnalysis()
                    .substring(0,
                        Math.min(200,
                            app.getAiAnalysis()
                                .length()))
                : "Not available");

        String sheet = geminiService.chat(
            systemPrompt, interviewNotes);

        // Save evaluation sheet
        app.setHrComment(sheet);
        applicationRepository.save(app);

        log.info("Evaluation sheet generated "
            + "for application id={}",
            applicationId);

        return sheet;
    }
}
