package com.mrfop.worktime.service;

import com.mrfop.worktime.exception.OperationBlockedByRunningException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.request.WorkReportDaysRequest;
import com.mrfop.worktime.model.request.WorkReportExportRequest;
import com.mrfop.worktime.model.response.WorkDayResponse;
import com.mrfop.worktime.service.jasper.JasperRenderer;
import com.mrfop.worktime.service.jasper.model.WorkReportPdf;
import com.mrfop.worktime.service.jasper.model.mapper.WorkReportPdfMapper;
import com.mrfop.worktime.util.ParsingUtil;
import com.mrfop.worktime.util.TimeRangeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WorkReportExportService {

    private final WorkReportService reportService;

    private final WorkSessionService sessionService;
    private final WorkSegmentService segmentService;

    private final WorkReportPdfMapper mapper;
    private final JasperRenderer renderer;

    public byte[] exportPdf(WorkReportExportRequest req) {
        Instant from = ParsingUtil.parseInstantIsoRequired(req.from(), Subject.WORK_REPORT, LookupField.FROM);
        Instant to   = ParsingUtil.parseInstantIsoRequired(req.to(),   Subject.WORK_REPORT, LookupField.TO);

        TimeRangeUtil.requireValidStrictClosedRange(Subject.WORK_REPORT, from, to);

        ZoneId zone  = ParsingUtil.parseZoneRequired(req.tz(), Subject.WORK_REPORT, LookupField.TZ);
        Locale locale = ParsingUtil.parseLocaleRequired(req.locale(), Subject.WORK_REPORT, LookupField.LOCALE);

        // Store in a local variable for clarity
        boolean includeSegments = req.showSegments();

        // 1) Guard: running?
        if (segmentService.isRunning()) {
            throw new OperationBlockedByRunningException(Subject.WORK_REPORT, Subject.WORK_SEGMENT);
        }
        if (sessionService.isRunning()) {
            throw new OperationBlockedByRunningException(Subject.WORK_REPORT, Subject.WORK_SESSION);
        }

        // 2) Data (UTC only)
        List<WorkDayResponse> days = reportService.daysWithSegments(new WorkReportDaysRequest(from, to));

        // 3) Map to PDF ViewModel (already formatted for tz/locale)
        WorkReportPdf model = mapper.toPdfModel(days, from, to, zone, locale, includeSegments);

        // 4) Render Jasper PDF
        return renderer.renderPdf(model);
    }

    public String buildFilename(WorkReportExportRequest req) {
        Instant from = ParsingUtil.parseInstantIsoRequired(req.from(), Subject.WORK_REPORT, LookupField.FROM);
        Instant to   = ParsingUtil.parseInstantIsoRequired(req.to(),   Subject.WORK_REPORT, LookupField.TO);
        ZoneId zone  = ParsingUtil.parseZoneRequired(req.tz(), Subject.WORK_REPORT, LookupField.TZ);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(zone);
        return "work-report_" + df.format(from) + "_" + df.format(to) + ".pdf";
    }
}