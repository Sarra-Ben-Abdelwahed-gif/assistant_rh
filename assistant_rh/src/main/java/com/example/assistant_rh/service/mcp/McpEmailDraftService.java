package com.example.assistant_rh.service.mcp;

import com.example.assistant_rh.entity.Application;
import com.example.assistant_rh.entity.JobOffer;
import com.example.assistant_rh.repository.ApplicationRepository;
import com.example.assistant_rh.service.EmailService;
import com.example.assistant_rh.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class McpEmailDraftService {

    private final GeminiService geminiService;
    private final EmailService emailService;
    private final ApplicationRepository
        applicationRepository;

    // Generate and send job offer
    // commitment email
    public String sendHireEmail(
            Long applicationId) {
        Application app = applicationRepository
            .findById(applicationId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Application not found"));

        JobOffer offer = app.getJobOffer();

        String prompt = String.format("""
            Write a professional job offer / job commitment
            email in English.
            
            Candidate: %s
            Position: %s
            Department: %s
            
            The email must be:
            - Warm and professional
            - Mention the exact job position
            - Ask for confirmation of availability
            - Signed "The HR Team"
            - Short (max 150 words)
            """,
            app.getCandidateName(),
            offer != null
                ? offer.getTitle() : "the position",
            offer != null
                ? offer.getDepartment() : "");

        String emailBody = geminiService.chat(
            prompt,
            "Generate the email now.");

        // Send the email
        try {
            emailService
                .sendApplicationConfirmation(
                    app.getCandidateEmail(),
                    app.getCandidateName(),
                    offer != null
                        ? offer.getTitle()
                        : "the position");

            log.info("Job offer email sent to {}",
                app.getCandidateEmail());

            return "✅ Job offer email "
                + "sent to "
                + app.getCandidateEmail()
                + "\n\nContent:\n" + emailBody;

        } catch (Exception e) {
            log.error("Email sending error: {}",
                e.getMessage());
            return "Generated email content "
                + "(sending failed):\n\n"
                + emailBody;
        }
    }

    // Generate and send a constructive,
    // personalized rejection email
    public String sendRejectionEmail(
            Long applicationId,
            String feedback) {
        Application app = applicationRepository
            .findById(applicationId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Application not found"));

        JobOffer offer = app.getJobOffer();

        String prompt = String.format("""
            Write a constructive and kind
            rejection email in English.
            
            Candidate: %s
            Position: %s
            HR Feedback: %s
            Initial Resume AI Score: %s/100
            
            The email must:
            - Sincerely thank the candidate
            - Explain the rejection in a positive way
            - Give 1-2 tips for improvement
            - Encourage them for the future
            - Leave the door open for future opportunities
            - Max 180 words
            """,
            app.getCandidateName(),
            offer != null
                ? offer.getTitle() : "the position",
            feedback != null
                ? feedback
                : "Interesting profile but "
                + "lacks sufficient experience",
            app.getScore() != null
                ? app.getScore() : "N/A");

        String emailBody = geminiService.chat(
            prompt,
            "Generate the rejection email now.");

        log.info("Rejection email generated for {}",
            app.getCandidateEmail());

        return "📧 Rejection email generated for "
            + app.getCandidateName()
            + " :\n\n" + emailBody
            + "\n\n💬 Reply 'Send' "
            + "to dispatch this email.";
    }

    // Generate interview invitation email
    public String sendInterviewInvite(
            Long applicationId,
            String dateTime,
            String location) {
        Application app = applicationRepository
            .findById(applicationId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Application not found"));

        JobOffer offer = app.getJobOffer();

        String prompt = String.format("""
            Write a professional interview invitation
            email in English.
            
            Candidate: %s
            Position: %s
            Date/Time: %s
            Location: %s
            
            The email must:
            - Confirm the interview invitation
            - Provide clear practical details
            - Request attendance confirmation
            - Provide an HR contact point
            - Formal yet warm tone
            - Max 150 words
            """,
            app.getCandidateName(),
            offer != null
                ? offer.getTitle() : "the position",
            dateTime,
            location);

        String emailBody = geminiService.chat(
            prompt,
            "Generate the invitation now.");

        return "📅 Invitation generated for "
            + app.getCandidateName()
            + " :\n\n" + emailBody
            + "\n\n💬 Reply 'Send' "
            + "to dispatch this invitation.";
    }
}
