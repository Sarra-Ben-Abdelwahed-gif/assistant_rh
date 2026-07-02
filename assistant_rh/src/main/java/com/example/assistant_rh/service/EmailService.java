package com.example.assistant_rh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    
    @Async
    public void sendLeaveRequestNotification(
            String adminEmail,
            String employeeName,
            String leaveType,
            String startDate,
            String endDate) {
        try {
            MimeMessage message =
                mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true,
                    "UTF-8");
            helper.setTo(adminEmail);
            helper.setSubject(
                "New leave request — "
                + employeeName);
            helper.setText(String.format("""
                <html><body>
                <h2>New leave request</h2>
                <p><b>Employee :</b> %s</p>
                <p><b>Type :</b> %s</p>
                <p><b>From :</b> %s</p>
                <p><b>To :</b> %s</p>
                <p>Please log in to process
                this request.</p>
                </body></html>
                """,
                employeeName, leaveType,
                startDate, endDate), true);
            mailSender.send(message);
            log.info("Email leave request sent to {}",
                adminEmail);
        } catch (Exception e) {
            log.error("Error sending email : {}",
                e.getMessage());
        }
    }

    
    @Async
    public void sendLeaveStatusNotification(
            String employeeEmail,
            String employeeName,
            String status,
            String adminComment) {
        try {
            MimeMessage message =
                mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true,
                    "UTF-8");
            helper.setTo(employeeEmail);
            String statusFr = status.equals("APPROVED")
                ? "✅ Approved" : "❌ Rejected";
            helper.setSubject(
                "Your leave request — " + statusFr);
            helper.setText(String.format("""
                <html><body>
                <h2>Response to your leave request</h2>
                <p>Hello %s,</p>
                <p>Your request has been : <b>%s</b></p>
                %s
                </body></html>
                """,
                employeeName, statusFr,
                adminComment != null
                    ? "<p><b>Comment :</b> "
                        + adminComment + "</p>"
                    : ""), true);
            mailSender.send(message);
            log.info("Email leave status sent to {}",
                employeeEmail);
        } catch (Exception e) {
            log.error("Error sending email : {}",
                e.getMessage());
        }
    }

    
    @Async
    public void sendApplicationConfirmation(
            String candidateEmail,
            String candidateName,
            String jobTitle) {
        try {
            MimeMessage message =
                mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true,
                    "UTF-8");
            helper.setTo(candidateEmail);
            helper.setSubject(
                "Application received — " + jobTitle);
            helper.setText(String.format("""
                <html><body>
                <h2>Application received!</h2>
                <p>Hello %s,</p>
                <p>Your application for the position
                <b>%s</b> has been received.</p>
                <p>We will get back to you
                shortly.</p>
                <p>Best regards,<br>The HR Team</p>
                </body></html>
                """,
                candidateName, jobTitle), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending email : {}",
                e.getMessage());
        }
    }

    
    @Async
    public void sendWelcomeEmail(
            String employeeEmail,
            String employeeName,
            String tempPassword) {
        try {
            MimeMessage message =
                mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true,
                    "UTF-8");
            helper.setTo(employeeEmail);
            helper.setSubject(
                "Welcome — Your access to the HR platform");
            helper.setText(String.format("""
                <html><body>
                <h2>Welcome %s !</h2>
                <p>Your account has been created.</p>
                <p><b>Email :</b> %s</p>
                <p><b>Temporary password :</b>
                %s</p>
                <p>Change your password upon your first login.</p>
                </body></html>
                """,
                employeeName, employeeEmail,
                tempPassword), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending email : {}",
                e.getMessage());
        }
    }
}