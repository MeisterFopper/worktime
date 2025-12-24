package com.mrfop.worktime.service.jasper.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SessionPdf {
    String txtStartLabel;
    String startLabel;

    String txtEndLabel;
    String endLabel;
    
    String txtDurationLabel;
    String durationLabel;
    
    String txtSegmentsLabel;
    String segmentsLabel;
    
    String txtUnallocatedLabel;
    String unallocatedLabel;
    
    List<SegmentPdf> segments;
}