package com.mrfop.worktime.model.response;

import java.time.Instant;

public record WorkSegmentResponse(
    Long id,
    Long workSessionId,
    Long categoryId,
    String categoryName,
    Long activityId,
    String activityName,
    Instant startTime,
    Instant endTime,
    String comment,
    Instant createdAt,
    Instant updatedAt
) {}