package com.example.assistant_rh.service;

import com.example.assistant_rh.entity.Employee;
import com.example.assistant_rh.entity.LeaveRequest;
import com.example.assistant_rh.exception.ResourceNotFoundException;
import com.example.assistant_rh.repository.EmployeeRepository;
import com.example.assistant_rh.repository.LeaveRequestRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRepository;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Attestation de travail
    public byte[] generateWorkCertificate(
            Long employeeId) {
        Employee emp = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee", "id", employeeId));

        try (ByteArrayOutputStream baos =
                new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            // Titre
            doc.add(new Paragraph("ATTESTATION DE TRAVAIL")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold()
                .setMarginBottom(30));

            // Contenu
            doc.add(new Paragraph(String.format("""
                Je soussigné(e), certifie que
                M./Mme %s %s occupe le poste de %s
                au sein du département %s
                depuis le %s.

                Cette attestation est délivrée
                à l'intéressé(e) pour faire valoir
                ce que de droit.
                """,
                emp.getFirstName(), emp.getLastName(),
                emp.getPosition(), emp.getDepartment(),
                emp.getHireDate().format(FORMATTER)))
                .setFontSize(12)
                .setMarginBottom(40));

            // Date et signature
            doc.add(new Paragraph(
                "Fait le " + LocalDate.now()
                    .format(FORMATTER))
                .setTextAlignment(TextAlignment.RIGHT));
            doc.add(new Paragraph(
                "Signature et cachet")
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(40));

            doc.close();
            log.info("Attestation générée : id={}",
                employeeId);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erreur PDF : {}", e.getMessage());
            throw new RuntimeException(
                "Erreur génération PDF : "
                + e.getMessage());
        }
    }

    // Fiche de congé
    public byte[] generateLeavePdf(Long leaveId) {
        LeaveRequest leave = leaveRepository
                .findById(leaveId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "LeaveRequest", "id", leaveId));
        Employee emp = leave.getEmployee();

        try (ByteArrayOutputStream baos =
                new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph(
                "FICHE DE DEMANDE DE CONGÉ")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginBottom(20));

            // Tableau infos
            Table table = new Table(
                UnitValue.createPercentArray(
                    new float[]{40, 60}))
                .useAllAvailableWidth();

            addRow(table, "Employé",
                emp.getFirstName() + " "
                + emp.getLastName());
            addRow(table, "Département",
                emp.getDepartment());
            addRow(table, "Poste", emp.getPosition());
            addRow(table, "Type de congé",
                leave.getType().toString());
            addRow(table, "Date de début",
                leave.getStartDate().format(FORMATTER));
            addRow(table, "Date de fin",
                leave.getEndDate().format(FORMATTER));
            addRow(table, "Nombre de jours",
                String.valueOf(leave.getNumberOfDays()));
            addRow(table, "Statut",
                leave.getStatus().toString());
            if (leave.getReason() != null)
                addRow(table, "Motif", leave.getReason());
            if (leave.getAdminComment() != null)
                addRow(table, "Commentaire RH",
                    leave.getAdminComment());

            doc.add(table);
            doc.add(new Paragraph(
                "Généré le "
                + LocalDate.now().format(FORMATTER))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(30)
                .setFontSize(10));

            doc.close();
            log.info("PDF congé généré : id={}", leaveId);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(
                "Erreur PDF congé : " + e.getMessage());
        }
    }

    // Liste des employés
    public byte[] generateEmployeeListPdf() {
        List<Employee> employees =
            employeeRepository.findAll();

        try (ByteArrayOutputStream baos =
                new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("LISTE DES EMPLOYÉS")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginBottom(20));

            doc.add(new Paragraph(
                "Généré le "
                + LocalDate.now().format(FORMATTER)
                + " — Total : "
                + employees.size() + " employé(s)")
                .setFontSize(10)
                .setMarginBottom(15));

            Table table = new Table(
                UnitValue.createPercentArray(
                    new float[]{20, 20, 20, 20, 20}))
                .useAllAvailableWidth();

            // En-têtes
            for (String header : new String[]{
                "Nom", "Email", "Département",
                "Poste", "Statut"}) {
                table.addHeaderCell(new Cell()
                    .add(new Paragraph(header)
                        .setBold())
                    .setBackgroundColor(
                        ColorConstants.LIGHT_GRAY));
            }

            // Données
            for (Employee emp : employees) {
                table.addCell(emp.getFirstName()
                    + " " + emp.getLastName());
                table.addCell(emp.getEmail());
                table.addCell(emp.getDepartment()
                    != null ? emp.getDepartment() : "-");
                table.addCell(emp.getPosition()
                    != null ? emp.getPosition() : "-");
                table.addCell(emp.getStatus()
                    .toString());
            }

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(
                "Erreur PDF liste : " + e.getMessage());
        }
    }

    private void addRow(Table table,
            String label, String value) {
        table.addCell(new Cell()
            .add(new Paragraph(label).setBold())
            .setBackgroundColor(
                ColorConstants.LIGHT_GRAY));
        table.addCell(new Cell()
            .add(new Paragraph(value)));
    }
}
