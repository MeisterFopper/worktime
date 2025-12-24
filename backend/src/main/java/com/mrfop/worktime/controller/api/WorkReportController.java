package com.mrfop.worktime.controller.api;

import com.mrfop.worktime.model.request.WorkReportDaysRequest;
import com.mrfop.worktime.model.request.WorkReportExportRequest;
import com.mrfop.worktime.model.response.WorkDayResponse;
import com.mrfop.worktime.service.WorkReportExportService;
import com.mrfop.worktime.service.WorkReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "5. Work Report API", description = "Report work sessions and segments")
@RequiredArgsConstructor
public class WorkReportController {

    private final WorkReportService reportService;
    private final WorkReportExportService exportService;

    @GetMapping("/days")
    @Operation(summary = "Get work sessions grouped by day including segments (UTC only)")
    public ResponseEntity<List<WorkDayResponse>> getDays(@Valid @ModelAttribute WorkReportDaysRequest req) {
        return ResponseEntity.ok(reportService.daysWithSegments(req));
    }

    @GetMapping(value = "/export.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Export report as PDF (no export if any session/segment is running)")
    public ResponseEntity<byte[]> exportPdf(@Valid @ModelAttribute WorkReportExportRequest req) {
        byte[] pdf = exportService.exportPdf(req);
        String filename = exportService.buildFilename(req);

        ContentDisposition cd = ContentDisposition
                .inline() // or .attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore().mustRevalidate())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(pdf);
    }
}