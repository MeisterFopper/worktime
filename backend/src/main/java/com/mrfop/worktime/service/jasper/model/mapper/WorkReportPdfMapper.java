package com.mrfop.worktime.service.jasper.model.mapper;

import com.mrfop.worktime.model.response.WorkDayResponse;
import com.mrfop.worktime.model.response.WorkSegmentResponse;
import com.mrfop.worktime.model.response.WorkSessionWithSegmentsResponse;
import com.mrfop.worktime.service.jasper.model.DayPdf;
import com.mrfop.worktime.service.jasper.model.SegmentPdf;
import com.mrfop.worktime.service.jasper.model.SessionPdf;
import com.mrfop.worktime.service.jasper.model.WorkReportPdf;
import com.mrfop.worktime.service.jasper.model.WorkReportPdfLabels;
import com.mrfop.worktime.util.StringSanitizer;
import com.mrfop.worktime.util.TimeFormatUtil;
import com.mrfop.worktime.util.TimeMathUtil;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class WorkReportPdfMapper {

    public WorkReportPdf toPdfModel(
            List<WorkDayResponse> days,
            Instant from,
            Instant to,
            ZoneId zone,
            Locale locale,
            boolean includeSegments
    ) {
        WorkReportPdfLabels labels = WorkReportPdfLabels.EN;

        // Prepare formatters
        DateTimeFormatter dateTimeFmt = TimeFormatUtil.dateTimeFormatter(zone, locale);
        DateTimeFormatter dayFmt = TimeFormatUtil.dayLabelFormatter(zone, locale);

        String periodLabel = TimeFormatUtil.formatPeriod(from, to, dateTimeFmt, labels.missingPlaceholder());
        String generatedAtLabel = TimeFormatUtil.formatGeneratedAt(Instant.now(), dateTimeFmt);

        List<WorkDayResponse> safeDays = (days == null) ? List.of() : days;

        // Build day models
        List<DayPdf> dayPdfs = safeDays.stream()
                .map(d -> mapDay(d, includeSegments, dateTimeFmt, dayFmt, labels))
                .toList();

        return WorkReportPdf.builder()
                .titleLabel(labels.titleWorkSessions())
                .txtPeriodLabel(labels.txtPeriodLabel())
                .periodLabel(periodLabel)
                .txtGeneratedAtLabel(labels.txtGeneratedAtLabel())
                .generatedAtLabel(generatedAtLabel)
                .txtZoneLabel(labels.txtZoneLabel())
                .zoneLabel(zone.getId())
                .days(dayPdfs)
                .build();
    }

    private DayPdf mapDay(
            WorkDayResponse day,
            boolean includeSegments,
            DateTimeFormatter dateTimeFmt,
            DateTimeFormatter dayFmt,
            WorkReportPdfLabels labels
    ) {
        List<WorkSessionWithSegmentsResponse> sessions = Optional.ofNullable(day.sessions()).orElse(List.of());

        long totalSessionSecs = 0;
        long totalSegmentSecs = 0;

        List<SessionPdf> sessionPdfs = new ArrayList<>(sessions.size());
        for (WorkSessionWithSegmentsResponse s : sessions) {
            SessionCalc calc = calcSession(s);
            totalSessionSecs += calc.sessionSecs;
            totalSegmentSecs += calc.segmentSecs;

            sessionPdfs.add(mapSession(s, calc, includeSegments, dateTimeFmt, labels));
        }

        long totalUnallocatedSecs = Math.max(0, totalSessionSecs - totalSegmentSecs);

        return DayPdf.builder()
                .txtDayLabel(labels.txtDayLabel())
                .dayLabel(TimeFormatUtil.formatDayLabel(day.dayUtc(), dayFmt))
                .txtTotalLabel(labels.txtTotalLabel())
                .totalLabel(TimeFormatUtil.formatDuration(totalSessionSecs))
                .txtSegmentsLabel(labels.txtSegmentsLabel())
                .segmentsLabel(TimeFormatUtil.formatDuration(totalSegmentSecs))
                .txtUnallocatedLabel(labels.txtUnallocatedLabel())
                .unallocatedLabel(TimeFormatUtil.formatDuration(totalUnallocatedSecs))
                .sessions(sessionPdfs)
                .build();
    }

    private SessionPdf mapSession(
            WorkSessionWithSegmentsResponse s,
            SessionCalc calc,
            boolean includeSegments,
            DateTimeFormatter dateTimeFmt,
            WorkReportPdfLabels labels
    ) {
        String startLabel = TimeFormatUtil.formatInstant(s.startTime(), dateTimeFmt);
        String endLabel = (s.endTime() != null) ? TimeFormatUtil.formatInstant(s.endTime(), dateTimeFmt) : "";

        List<SegmentPdf> segments = includeSegments
                ? mapSegments(Optional.ofNullable(s.items()).orElse(List.of()), dateTimeFmt, labels)
                : List.of();

        return SessionPdf.builder()
            .txtStartLabel(labels.txtStartLabel())
            .startLabel(startLabel)
            .txtEndLabel(labels.txtEndLabel())
            .endLabel(endLabel)
            .txtDurationLabel(labels.txtDurationLabel())
            .durationLabel(TimeFormatUtil.formatDuration(calc.sessionSecs))
            .txtSegmentsLabel(labels.txtSegmentsLabel())
            .segmentsLabel(TimeFormatUtil.formatDuration(calc.segmentSecs))
            .txtUnallocatedLabel(labels.txtUnallocatedLabel())
            .unallocatedLabel(TimeFormatUtil.formatDuration(calc.unallocatedSecs))
            .segments(segments)
            .build();
    }

    private List<SegmentPdf> mapSegments(List<WorkSegmentResponse> items, DateTimeFormatter dateTimeFmt, WorkReportPdfLabels labels) {
        if (items == null || items.isEmpty()) return List.of();

        return items.stream()
                .map(seg -> {
                    long secs = TimeMathUtil.diffSeconds(seg.startTime(), seg.endTime());
                    String startLabel = TimeFormatUtil.formatInstant(seg.startTime(), dateTimeFmt);
                    String endLabel = (seg.endTime() != null) ? TimeFormatUtil.formatInstant(seg.endTime(), dateTimeFmt) : "";

                    return SegmentPdf.builder()
                        .txtStartLabel(labels.txtStartLabel())
                        .startLabel(startLabel)
                        .txtEndLabel(labels.txtEndLabel())
                        .endLabel(endLabel)
                        .txtDurationLabel(labels.txtDurationLabel())
                        .durationLabel(TimeFormatUtil.formatDuration(secs))
                        .txtCategoryLabel(labels.txtCategoryLabel())
                        .categoryLabel(seg.categoryName())
                        .txtActivityLabel(labels.txtActivityLabel())
                        .activityLabel(seg.activityName())
                        .txtCommentLabel(labels.txtCommentLabel())
                        .commentLabel(StringSanitizer.normalizeNonNull(seg.comment()))
                        .build();
                })
                .toList();
    }

    private static SessionCalc calcSession(WorkSessionWithSegmentsResponse s) {
        Instant start = s.startTime();
        Instant end = s.endTime();

        long sessionSecs = TimeMathUtil.diffSeconds(start, end);

        List<WorkSegmentResponse> items = (s.items() == null) ? List.of() : s.items();
        long segmentSecs = items.stream()
                .mapToLong(seg -> TimeMathUtil.diffSeconds(seg.startTime(), seg.endTime()))
                .sum();

        long unallocatedSecs = Math.max(0, sessionSecs - segmentSecs);
        return new SessionCalc(sessionSecs, segmentSecs, unallocatedSecs);
    }

    private record SessionCalc(long sessionSecs, long segmentSecs, long unallocatedSecs) {}
}