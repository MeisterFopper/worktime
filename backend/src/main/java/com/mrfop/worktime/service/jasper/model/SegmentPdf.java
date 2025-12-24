package com.mrfop.worktime.service.jasper.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SegmentPdf {
    String txtStartLabel;
    String startLabel;

    String txtEndLabel;
    String endLabel;

    String txtDurationLabel;
    String durationLabel;

    String txtCategoryLabel;
    String categoryLabel;

    String txtActivityLabel;
    String activityLabel;
    
    String txtCommentLabel;
    String commentLabel;
}