package com.mrfop.worktime.service.jasper.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class WorkReportPdf {
    String titleLabel;

    String txtPeriodLabel;
    String periodLabel;
    
    String txtGeneratedAtLabel;
    String generatedAtLabel;
    
    String txtZoneLabel;
    String zoneLabel;
    
    List<DayPdf> days;
}