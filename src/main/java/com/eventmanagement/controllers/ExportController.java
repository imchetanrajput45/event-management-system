package com.eventmanagement.controllers;

import com.eventmanagement.services.ExportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.ByteArrayInputStream;
import java.io.IOException;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    // ✅ Export Events to Excel
    @GetMapping("/events/export/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<InputStreamResource> exportEventsToExcel() throws IOException {
        ByteArrayInputStream excelData = exportService.exportEventsToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=events.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(excelData));
    }



    // ✅ Export Events to PDF
    @GetMapping("/events/export/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<InputStreamResource> exportEventsToPDF() {
        ByteArrayInputStream pdfData = exportService.exportEventsToPDF();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=events.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfData));
    }


    @GetMapping("/attendance/export/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<InputStreamResource> exportAttendanceToExcel(@RequestParam Long eventId) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // ✅ Get logged-in organizer's username

        ByteArrayInputStream excelData = exportService.exportAttendanceToExcel(eventId, username);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=attendance_" + eventId + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(excelData));
    }

    @GetMapping("/attendance/export/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<InputStreamResource> exportAttendanceToPDF(@RequestParam Long eventId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // ✅ Get logged-in organizer's username

        ByteArrayInputStream pdfData = exportService.exportAttendanceToPDF(eventId, username);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=attendance_" + eventId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfData));
    }






}
