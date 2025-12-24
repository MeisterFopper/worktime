package com.mrfop.worktime.service.jasper.model;

/**
 * English labels used in the Work Report PDF.
 *
 * <p>Centralizes all PDF/UI strings so the mapper remains focused on mapping logic.
 * Localization is intentionally not implemented yet.</p>
 */
public record WorkReportPdfLabels(
        // Report header
        String titleWorkSessions,
        String txtPeriodLabel,
        String txtGeneratedAtLabel,
        String txtZoneLabel,

        // Day block
        String txtDayLabel,
        String txtTotalLabel,
        String txtSegmentsLabel,
        String txtUnallocatedLabel,

        // Session block
        String txtStartLabel,
        String txtEndLabel,
        String txtDurationLabel,

        // Segment block
        String txtCategoryLabel,
        String txtActivityLabel,
        String txtCommentLabel,

        // Generic
        String missingPlaceholder
) {
    public static final WorkReportPdfLabels EN = new WorkReportPdfLabels(
            // Report header
            "Work Sessions",
            "Period:",
            "Generated:",
            "Time Zone:",

            // Day block
            "Day:",
            "Total:",
            "Segments:",
            "Unallocated:",

            // Session block
            "Start:",
            "End:",
            "Duration:",

            // Segment block
            "Category:",
            "Activity:",
            "Comment:",

            // Generic
            "—"
    );
}