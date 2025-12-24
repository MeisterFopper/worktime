package com.mrfop.worktime.model.request;

import jakarta.validation.constraints.Size;

public record WorkSegmentStopRequest(
    Long categoryId,
    Long activityId,
    @Size(max = 500) String comment
) {}