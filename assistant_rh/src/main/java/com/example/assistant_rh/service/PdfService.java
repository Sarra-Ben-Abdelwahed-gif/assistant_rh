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

            
            doc.add(new Paragraph("Work Certificate")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold()
                .setMarginBottom(30));

            
            doc.add(new Paragraph(String.format("""
                I, the undersigned, certify that
                Mr./Ms. %s %s holds the position of %s
                within the %s department
                since %s.

                This certificate is issued
                to the interested party for all
                legal intents and purposes.
                """,
                emp.getFirstName(), emp.getLastName(),
                emp.getPosition(), emp.getDepartment(),
                emp.getHireDate().format(FORMATTER)))
                .setFontSize(12)
                .setMarginBottom(40));

            
            doc.add(new Paragraph(
                "Issued on " + LocalDate.now()
                    .format(FORMATTER))
                .setTextAlignment(TextAlignment.RIGHT));
            doc.add(new Paragraph(
                "Signature and stamp")
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(40));

            doc.close();
            log.info("Certificate generated: id={}",
                employeeId);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF error: {}", e.getMessage());
            throw new RuntimeException(
                "PDF generation error : "
                + e.getMessage());
        }
    }

    
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
                "LEAVE REQUEST FORM")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginBottom(20));

            
            Table table = new Table(
                UnitValue.createPercentArray(
                    new float[]{40, 60}))
                .useAllAvailableWidth();

            addRow(table, "Employee",
                emp.getFirstName() + " "
                + emp.getLastName());
            addRow(table, "Department",
                emp.getDepartment());
            addRow(table, "Position", emp.getPosition());
            addRow(table, "Leave type",
                leave.getType().toString());
            addRow(table, "Start date",
                leave.getStartDate().format(FORMATTER));
            addRow(table, "End date",
                leave.getEndDate().format(FORMATTER));
            addRow(table, "Number of days",
                String.valueOf(leave.getNumberOfDays()));
            addRow(table, "Status",
                leave.getStatus().toString());
            if (leave.getReason() != null)
                addRow(table, "Reason", leave.getReason());
            if (leave.getAdminComment() != null)
                addRow(table, "HR comment",
                    leave.getAdminComment());

            doc.add(table);
            doc.add(new Paragraph(
                "Generated on "
                + LocalDate.now().format(FORMATTER))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(30)
                .setFontSize(10));

            doc.close();
            log.info("Leave PDF generated: id={}", leaveId);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(
                "Leave PDF error : " + e.getMessage());
        }
    }

    
    public byte[] generateEmployeeListPdf() {
        List<Employee> employees =
            employeeRepository.findAll();

        try (ByteArrayOutputStream baos =
                new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("EMPLOYEE LIST")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginBottom(20));

            doc.add(new Paragraph(
                "Done on "
                + LocalDate.now().format(FORMATTER)
                + " — Total : "
                + employees.size() + " employee(s)")
                .setFontSize(10)
                .setMarginBottom(15));

            Table table = new Table(
                UnitValue.createPercentArray(
                    new float[]{20, 20, 20, 20, 20}))
                .useAllAvailableWidth();

            // En-têtes
            for (String header : new String[]{
                "Name", "Email", "Department",
                "Position", "Status"}) {
                table.addHeaderCell(new Cell()
                    .add(new Paragraph(header)
                        .setBold())
                    .setBackgroundColor(
                        ColorConstants.LIGHT_GRAY));
            }

            
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
                "PDF error : " + e.getMessage());
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
