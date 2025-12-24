package com.mrfop.worktime.model.response;

import java.time.Instant;
import java.util.List;

public record WorkSessionWithSegmentsResponse(
    Long id,
    Instant startTime,
    Instant endTime,
    List<WorkSegmentResponse> items
) {}