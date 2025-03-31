package com.eventmanagement.services;

import com.eventmanagement.entities.Event;
import com.eventmanagement.entities.EventRegistration;
import com.eventmanagement.repositories.EventRepository;
import com.eventmanagement.repositories.EventRegistrationRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

@Service
public class ExportService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    public ExportService(EventRepository eventRepository, EventRegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    // ✅ Export Events to Excel
    public ByteArrayInputStream exportEventsToExcel() throws IOException {
        List<Event> events = eventRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Events");

        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Name", "Date", "Time", "Location", "Organizer"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);  // ✅ Apache POI Cell
        }

        int rowIdx = 1;
        for (Event event : events) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(String.valueOf(event.getId()));
            row.createCell(1).setCellValue(event.getName());
            row.createCell(2).setCellValue(event.getDate().toString());
            row.createCell(3).setCellValue(event.getTime().toString());
            row.createCell(4).setCellValue(event.getLocation());
            row.createCell(5).setCellValue(event.getOrganizer().getUsername());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ✅ Export Events to PDF
    public ByteArrayInputStream exportEventsToPDF() {
        List<Event> events = eventRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Event List").setBold().setFontSize(18));

            Table table = new Table(6);

            // ✅ Use Paragraph for headers (iText 7 does not support direct Cell instantiation)
            table.addHeaderCell(new Paragraph("ID"));
            table.addHeaderCell(new Paragraph("Name"));
            table.addHeaderCell(new Paragraph("Date"));
            table.addHeaderCell(new Paragraph("Time"));
            table.addHeaderCell(new Paragraph("Location"));
            table.addHeaderCell(new Paragraph("Organizer"));

            for (Event event : events) {
                table.addCell(new Paragraph(String.valueOf(event.getId())));
                table.addCell(new Paragraph(event.getName()));
                table.addCell(new Paragraph(event.getDate().toString()));
                table.addCell(new Paragraph(event.getTime().toString()));
                table.addCell(new Paragraph(event.getLocation()));
                table.addCell(new Paragraph(event.getOrganizer().getUsername()));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }


    public ByteArrayInputStream exportAttendanceToExcel(Long eventId, String organizerUsername) throws IOException {
        List<EventRegistration> registrations = registrationRepository.findByEventIdAndEventOrganizerUsernameAndAttendedTrue(eventId, organizerUsername);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance");

        Row headerRow = sheet.createRow(0);
        String[] columns = {"User Name", "Email", "Registered Date"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        int rowIdx = 1;
        for (EventRegistration reg : registrations) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(reg.getUser().getUsername());
            row.createCell(1).setCellValue(reg.getUser().getEmail());
            row.createCell(2).setCellValue(reg.getRegistrationDate().toString());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }



    public ByteArrayInputStream exportAttendanceToPDF(Long eventId, String organizerUsername) {
        List<EventRegistration> registrations = registrationRepository.findByEventIdAndEventOrganizerUsernameAndAttendedTrue(eventId, organizerUsername);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Attendance Report").setBold().setFontSize(18));

            Table table = new Table(3);
            table.addHeaderCell(new Paragraph("User Name"));
            table.addHeaderCell(new Paragraph("Email"));
            table.addHeaderCell(new Paragraph("Registered Date"));

            for (EventRegistration reg : registrations) {
                table.addCell(new Paragraph(reg.getUser().getUsername()));
                table.addCell(new Paragraph(reg.getUser().getEmail()));
                table.addCell(new Paragraph(reg.getRegistrationDate().toString()));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }






}
