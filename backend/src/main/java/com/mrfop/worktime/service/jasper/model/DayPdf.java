package com.mrfop.worktime.service.jasper.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DayPdf {
    String txtDayLabel;
    String dayLabel;

    String txtTotalLabel;
    String totalLabel;

    String txtSegmentsLabel;
    String segmentsLabel;

    String txtUnallocatedLabel;
    String unallocatedLabel;

    List<SessionPdf> sessions;
}