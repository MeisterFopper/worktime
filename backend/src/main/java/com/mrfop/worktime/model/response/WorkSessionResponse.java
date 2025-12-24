package com.mrfop.worktime.model.response;

import java.time.Instant;

public record WorkSessionResponse(
    Long id,
    Instant startTime,
    Instant endTime,
    Instant createdAt,
    Instant updatedAt
) {}