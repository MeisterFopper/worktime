// com.mrfop.worktime.model.request.WorkReportExportRequest
package com.mrfop.worktime.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkReportExportRequest(
    @NotBlank String from,
    @NotBlank String to,
    @NotBlank String tz,
    @NotBlank String locale,
    @NotNull Boolean showSegments
) {}