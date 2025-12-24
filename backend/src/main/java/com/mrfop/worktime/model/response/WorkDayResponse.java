package com.mrfop.worktime.model.response;

import java.time.LocalDate;
import java.util.List;

public record WorkDayResponse(
    LocalDate dayUtc,
    List<WorkSessionWithSegmentsResponse> sessions
) {}