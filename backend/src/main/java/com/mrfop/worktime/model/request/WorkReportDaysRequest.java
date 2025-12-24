package com.mrfop.worktime.model.request;

import com.mrfop.worktime.util.TimeRangeUtil;
import jakarta.validation.constraints.AssertTrue;

import java.time.Instant;

public record WorkReportDaysRequest(
        Instant from,
        Instant to
) {
    @AssertTrue(message = "to must be after or equal to from")
    public boolean orderOk() {
        return TimeRangeUtil.isValidOpenRange(from, to);
    }

    public boolean hasFrom() { return from != null; }
    public boolean hasTo() { return to != null; }
}