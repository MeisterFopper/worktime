package com.mrfop.worktime.model.request;

import com.mrfop.worktime.util.TimeRangeUtil;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record WorkSegmentPatchRequest(
        Long categoryId,
        Long activityId,
        Instant startTime,
        Instant endTime,
        @Size(max = 500) String comment
) {
    @AssertTrue(message = "Provide at least one field to patch")
    public boolean hasAny() {
        return categoryId != null
                || activityId != null
                || startTime != null
                || endTime != null
                || comment != null;
    }

    @AssertTrue(message = "endTime must be after or equal to startTime")
    public boolean orderOk() {
        return TimeRangeUtil.isValidOpenRange(startTime, endTime);
    }
}