package com.mrfop.worktime.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkSegmentStartRequest(
    @NotNull Long categoryId,
    @NotNull Long activityId,
    @Size(max = 500) String comment
) {}