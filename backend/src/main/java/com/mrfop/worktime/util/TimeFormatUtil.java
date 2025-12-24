package com.mrfop.worktime.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Formatting helpers for Work Report PDF rendering.
 *
 * <p>Centralizes date/time and duration formatting so mappers/services can focus on data mapping
 * and calculations. All formatters are created per call to ensure correct {@link Locale} and {@link ZoneId}
 * are applied, while still avoiding repeated formatter instantiation inside loops.</p>
 *
 * <p>All methods are null-safe and return empty strings (or a configurable placeholder) where appropriate.</p>
 */
public final class TimeFormatUtil {

    /**
     * Prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private TimeFormatUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Align with frontend style
    public static final String DATE_TIME_FMT = "dd.MM.yy HH:mm:ss";
    public static final String DAY_LABEL_FMT = "EEEE, dd.MM.yyyy";

    /**
     * Creates a formatter for "date time" values (e.g. session/segment start/end).
     */
    public static DateTimeFormatter dateTimeFormatter(ZoneId zone, Locale locale) {
        return DateTimeFormatter.ofPattern(DATE_TIME_FMT)
                .withLocale(Objects.requireNonNull(locale, "locale"))
                .withZone(Objects.requireNonNull(zone, "zone"));
    }

    /**
     * Creates a formatter for day labels (weekday + date).
     */
    public static DateTimeFormatter dayLabelFormatter(ZoneId zone, Locale locale) {
        return DateTimeFormatter.ofPattern(DAY_LABEL_FMT)
                .withLocale(Objects.requireNonNull(locale, "locale"))
                .withZone(Objects.requireNonNull(zone, "zone"));
    }

    /**
     * Formats an {@link Instant} using the provided formatter.
     */
    public static String formatInstant(Instant instant, DateTimeFormatter fmt) {
        if (instant == null) return "";
        return fmt.format(instant);
    }

    /**
     * Formats a {@link LocalDate} day marker (stored as UTC day) into a localized day label.
     *
     * <p>UI-aligned behavior: treat {@code dayUtc} as UTC midnight instant, then format in the
     * requested {@link ZoneId}.</p>
     */
    public static String formatDayLabel(LocalDate dayUtc, DateTimeFormatter dayFmt) {
        if (dayUtc == null) return "";
        Instant utcMidnight = dayUtc.atStartOfDay(ZoneOffset.UTC).toInstant();
        return dayFmt.format(utcMidnight);
    }

    /**
     * Builds a period label like "01.01.2025 00:00 – 31.01.2025 23:59".
     */
    public static String formatPeriod(Instant from, Instant to, DateTimeFormatter periodFmt, String missingPlaceholder) {
        String a = (from != null) ? periodFmt.format(from) : missingPlaceholder;
        String b = (to != null) ? periodFmt.format(to) : missingPlaceholder;
        return a + " - " + b;
    }

    /**
     * Builds a "generated at" label using current time.
     */
    public static String formatGeneratedAt(Instant now, DateTimeFormatter dateTimeFmt) {
        return dateTimeFmt.format(Objects.requireNonNull(now, "now"));
    }

    /**
     * Formats a duration in seconds to "Xh YYm ZZs".
     */
    public static String formatDuration(long secs) {
        long total = Math.max(0, secs);
        long h = total / 3600;
        long m = (total % 3600) / 60;
        long s = total % 60;
        return h + "h " + String.format("%02dm %02ds", m, s);
    }

    /**
     * Joins non-blank string parts with a separator.
     *
     * <p>Null parts are treated as empty. Each part is trimmed; blank parts are omitted.</p>
     */
    public static String joinNonBlank(String sep, String... parts) {
        return Arrays.stream(parts == null ? new String[0] : parts)
                .map(StringSanitizer::normalizeNonNull)
                .filter(v -> !v.isBlank())
                .collect(Collectors.joining(sep));
    }
}