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

    // Email congé soumis → à l'admin
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
                "Nouvelle demande de congé — "
                + employeeName);
            helper.setText(String.format("""
                <html><body>
                <h2>Nouvelle demande de congé</h2>
                <p><b>Employé :</b> %s</p>
                <p><b>Type :</b> %s</p>
                <p><b>Du :</b> %s</p>
                <p><b>Au :</b> %s</p>
                <p>Connectez-vous pour traiter
                cette demande.</p>
                </body></html>
                """,
                employeeName, leaveType,
                startDate, endDate), true);
            mailSender.send(message);
            log.info("Email congé envoyé à {}",
                adminEmail);
        } catch (Exception e) {
            log.error("Erreur email : {}",
                e.getMessage());
        }
    }

    // Email réponse congé → à l'employé
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
                ? "✅ Approuvée" : "❌ Rejetée";
            helper.setSubject(
                "Votre demande de congé — " + statusFr);
            helper.setText(String.format("""
                <html><body>
                <h2>Réponse à votre demande de congé</h2>
                <p>Bonjour %s,</p>
                <p>Votre demande a été : <b>%s</b></p>
                %s
                </body></html>
                """,
                employeeName, statusFr,
                adminComment != null
                    ? "<p><b>Commentaire :</b> "
                        + adminComment + "</p>"
                    : ""), true);
            mailSender.send(message);
            log.info("Email statut congé envoyé à {}",
                employeeEmail);
        } catch (Exception e) {
            log.error("Erreur email : {}",
                e.getMessage());
        }
    }

    // Email candidature reçue → au candidat
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
                "Candidature reçue — " + jobTitle);
            helper.setText(String.format("""
                <html><body>
                <h2>Candidature bien reçue !</h2>
                <p>Bonjour %s,</p>
                <p>Votre candidature pour le poste
                <b>%s</b> a bien été reçue.</p>
                <p>Nous reviendrons vers vous
                prochainement.</p>
                <p>Cordialement,<br>L'équipe RH</p>
                </body></html>
                """,
                candidateName, jobTitle), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Erreur email : {}",
                e.getMessage());
        }
    }

    // Email bienvenue → nouvel employé
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
                "Bienvenue — Vos accès à la plateforme RH");
            helper.setText(String.format("""
                <html><body>
                <h2>Bienvenue %s !</h2>
                <p>Votre compte a été créé.</p>
                <p><b>Email :</b> %s</p>
                <p><b>Mot de passe temporaire :</b>
                %s</p>
                <p>Changez votre mot de passe
                à la première connexion.</p>
                </body></html>
                """,
                employeeName, employeeEmail,
                tempPassword), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Erreur email : {}",
                e.getMessage());
        }
    }
}