package com.mrfop.worktime.model.request;

import com.mrfop.worktime.util.TimeRangeUtil;
import jakarta.validation.constraints.AssertTrue;

import java.time.Instant;

public record WorkSessionPatchRequest(Instant startTime, Instant endTime) {

    @AssertTrue(message = "Provide at least one of startTime or endTime")
    public boolean hasAny() {
        return startTime != null || endTime != null;
    }

    @AssertTrue(message = "endTime must be after or equal to startTime")
    public boolean orderOk() {
        return TimeRangeUtil.isValidOpenRange(startTime, endTime);
    }
}