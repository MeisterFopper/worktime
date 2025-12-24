package com.mrfop.worktime.service;

import com.mrfop.worktime.model.entity.WorkSessionEntity;
import com.mrfop.worktime.model.mapper.WorkSegmentMapper;
import com.mrfop.worktime.model.request.WorkReportDaysRequest;
import com.mrfop.worktime.model.response.WorkDayResponse;
import com.mrfop.worktime.model.response.WorkSegmentResponse;
import com.mrfop.worktime.model.response.WorkSessionWithSegmentsResponse;
import com.mrfop.worktime.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkReportService {

    private final WorkSessionRepository sessionRepo;
    private final WorkSegmentMapper segmentMapper;

    @Transactional(readOnly = true)
    public List<WorkDayResponse> daysWithSegments(WorkReportDaysRequest req) {
        Instant nowUtc = Instant.now();

        List<WorkSessionEntity> sessions =
                sessionRepo.findOverlappingWithSegments(nowUtc, req.from(), req.to());

        if (sessions.isEmpty()) return List.of();

        Map<LocalDate, List<WorkSessionWithSegmentsResponse>> byDay = new LinkedHashMap<>();
        
        for (WorkSessionEntity ws : sessions) {
            List<WorkSegmentResponse> items = ws.getSegments().stream()
                    .map(segmentMapper::toResponse)
                    .toList();

            WorkSessionWithSegmentsResponse s = new WorkSessionWithSegmentsResponse(
                    ws.getId(),
                    ws.getStartTime(),
                    ws.getEndTime(),
                    items
            );

            LocalDate dayUtc = ws.getStartDate();

            byDay.computeIfAbsent(dayUtc, k -> new ArrayList<>()).add(s);
        }

        return byDay.entrySet().stream()
                .map(e -> new WorkDayResponse(e.getKey(), e.getValue()))
                .toList();
    }
}