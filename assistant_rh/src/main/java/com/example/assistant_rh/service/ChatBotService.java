package com.example.assistant_rh.service;

import com.example.assistant_rh.dto.response.ChatResponse;
import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.enums.LeaveStatus;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotService {

    
    private final GeminiService geminiService;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRepository;

    public ChatResponse respond(String message,
            List<Map<String, String>> history) {
        String email = getCurrentEmail();
        String context = buildContext(email);
        String systemPrompt = buildSystemPrompt(context);

        
        String reply = geminiService.chatWithHistory(
            systemPrompt, history, message);

        log.info("Gemini ChatBot répondu pour : {}",
            email);
        return new ChatResponse(reply, "assistant");
    }

    public ChatResponse respondSimple(String message) {
        String email = getCurrentEmail();
        String context = buildContext(email);
        String systemPrompt = buildSystemPrompt(context);

        
        String reply = geminiService.chat(
            systemPrompt, message);
        return new ChatResponse(reply, "assistant");
    }

    private String buildContext(String email) {
        Optional<Employee> optEmployee =
            employeeRepository.findByEmail(email);

        if (optEmployee.isEmpty())
            return "Utilisateur connecté : "
                + email + " (Administrateur RH)";

        Employee emp = optEmployee.get();
        List<LeaveRequest> leaves =
            leaveRepository.findByEmployeeId(
                emp.getId());

        long pending = leaves.stream()
            .filter(l -> l.getStatus()
                == LeaveStatus.PENDING).count();
        long approved = leaves.stream()
            .filter(l -> l.getStatus()
                == LeaveStatus.APPROVED).count();

        return String.format("""
            Employé connecté :
            - Nom complet     : %s %s
            - Email           : %s
            - Département     : %s
            - Poste           : %s
            - Date d'embauche : %s
            - Statut          : %s
            - Solde congés    : %d jours disponibles
            - Congés en attente  : %d
            - Congés approuvés   : %d
            """,
            emp.getFirstName(), emp.getLastName(),
            emp.getEmail(),
            emp.getDepartment(),
            emp.getPosition(),
            emp.getHireDate(),
            emp.getStatus(),
            emp.getAnnualLeaveBalance(),
            pending, approved);
    }

    private String buildSystemPrompt(String context) {
        return """
            Tu es un assistant RH intelligent
            et professionnel.
            Tu aides les employés et managers RH.

            Tes responsabilités :
            - Informer sur les politiques de congés
            - Expliquer les procédures administratives
            - Aider avec les questions RH générales
            - Orienter vers le bon interlocuteur

            Règles :
            - Réponds toujours en français
            - Sois professionnel et bienveillant
            - Ne divulgue jamais les infos d'autrui
            - Max 3-4 paragraphes par réponse

            Contexte utilisateur :
            """ + context;
    }

    private String getCurrentEmail() {
        return SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();
    }
}
