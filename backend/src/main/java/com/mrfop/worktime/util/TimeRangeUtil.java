package com.mrfop.worktime.util;

import java.time.Instant;

import com.mrfop.worktime.exception.InvalidTimeRangeException;
import com.mrfop.worktime.exception.MissingTimeValueException;
import com.mrfop.worktime.exception.base.Subject;

/**
 * Time-related validation helpers for {@link Instant} intervals used throughout the application.
 *
 * <p>This utility distinguishes between two concepts in time-tracking:</p>
 *
 * <ul>
 *   <li><b>Open range</b> (a.k.a. "running" interval): {@code endTime} may be {@code null}
 *       (e.g., an active work session or active work segment). Missing values are tolerated; only
 *       an orderedness violation is rejected when both timestamps are present.</li>
 *   <li><b>Closed range</b> (a.k.a. "finished" interval): both {@code startTime} and {@code endTime}
 *       must be present and ordered. Equality is allowed ({@code endTime == startTime}).</li>
 *   <li><b>Strict closed range</b>: both values must be present and {@code endTime} must be strictly after
 *       {@code startTime} (equality is rejected). This is useful for requests that require a positive-duration window,
 *       such as report/export ranges.</li>
 * </ul>
 *
 * <h2>Recommended usage</h2>
 * <ul>
 *   <li>Use {@link #isValidOpenRange(Instant, Instant)} / {@link #requireValidOpenRange(Subject, Instant, Instant)}
 *       for PATCH requests and entities where {@code endTime} can be {@code null}.</li>
 *   <li>Use {@link #isValidClosedRange(Instant, Instant)} / {@link #requireValidClosedRange(Subject, Instant, Instant)}
 *       when a completed interval is required (e.g., computing a final duration). Zero-length intervals are allowed.</li>
 *   <li>Use {@link #isValidStrictClosedRange(Instant, Instant)} / {@link #requireValidStrictClosedRange(Subject, Instant, Instant)}
 *       when a positive-duration interval is required (e.g., export/report query ranges). Zero-length intervals are rejected.</li>
 * </ul>
 *
 * <h2>Error semantics</h2>
 * <ul>
 *   <li>{@link InvalidTimeRangeException}: the provided timestamps violate ordering rules.
 *       For closed ranges this means {@code endTime} is before {@code startTime}.
 *       For strict closed ranges this means {@code endTime} is not after {@code startTime} (before or equal).</li>
 *   <li>{@link MissingTimeValueException}: a (strict) closed range is required but {@code startTime}
 *       and/or {@code endTime} is {@code null}.</li>
 * </ul>
 */
public final class TimeRangeUtil {

    /**
     * Prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private TimeRangeUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks whether the given interval is valid as an <b>open range</b>.
     *
     * <p>An open range is valid if:</p>
     * <ul>
     *   <li>{@code startTime} is {@code null}, or</li>
     *   <li>{@code endTime} is {@code null}, or</li>
     *   <li>both are non-null and {@code endTime} is equal to or after {@code startTime}.</li>
     * </ul>
     *
     * <p>Use this when {@code endTime} may be absent (running session/segment) and you only
     * want to reject cases where both timestamps are present and out of order.</p>
     */
    public static boolean isValidOpenRange(Instant startTime, Instant endTime) {
        return startTime == null || endTime == null || !endTime.isBefore(startTime);
    }

    /**
     * Enforces that the given interval is valid as an <b>open range</b>.
     *
     * <p>Throws {@link InvalidTimeRangeException} only when both values are present and {@code endTime}
     * is before {@code startTime}. Missing values are allowed for open ranges.</p>
     *
     * @param subject   the domain subject (e.g., WORK_SESSION, WORK_SEGMENT) for error reporting
     * @param startTime the start time (may be {@code null})
     * @param endTime   the end time (may be {@code null})
     * @throws InvalidTimeRangeException if both values are provided and {@code endTime} is before {@code startTime}
     */
    public static void requireValidOpenRange(Subject subject, Instant startTime, Instant endTime) {
        if (!isValidOpenRange(startTime, endTime)) {
            throw new InvalidTimeRangeException(subject, startTime, endTime);
        }
    }

    /**
     * Checks whether the given interval is valid as a <b>closed range</b>.
     *
     * <p>A closed range is valid only if:</p>
     * <ul>
     *   <li>{@code startTime} is non-null, and</li>
     *   <li>{@code endTime} is non-null, and</li>
     *   <li>{@code endTime} is equal to or after {@code startTime} (equality is allowed).</li>
     * </ul>
     *
     * <h3>Examples</h3>
     * <ul>
     *   <li>{@code 10:00 – 10:00} is valid (0 seconds)</li>
     *   <li>{@code 10:00 – 09:59} is invalid</li>
     * </ul>
     */
    public static boolean isValidClosedRange(Instant startTime, Instant endTime) {
        return startTime != null && endTime != null && !endTime.isBefore(startTime);
    }

    /**
     * Enforces that the given interval is valid as a <b>closed range</b>.
     *
     * <p>A closed range requires both timestamps to be present and ordered. Equality is allowed
     * ({@code endTime == startTime}).</p>
     *
     * <p>Throws:</p>
     * <ul>
     *   <li>{@link MissingTimeValueException} if {@code startTime} or {@code endTime} is {@code null}</li>
     *   <li>{@link InvalidTimeRangeException} if both values are present but {@code endTime} is before {@code startTime}</li>
     * </ul>
     *
     * <h3>Examples</h3>
     * <ul>
     *   <li>{@code 10:00 – 10:00} is valid (0 seconds)</li>
     *   <li>{@code 10:00 – 09:59} is invalid</li>
     * </ul>
     *
     * @param subject   the domain subject (e.g., WORK_SESSION, WORK_SEGMENT) for error reporting
     * @param startTime the start time (must not be {@code null})
     * @param endTime   the end time (must not be {@code null})
     * @throws MissingTimeValueException if {@code startTime} or {@code endTime} is {@code null}
     * @throws InvalidTimeRangeException if both values are present and {@code endTime} is before {@code startTime}
     */
    public static void requireValidClosedRange(Subject subject, Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new MissingTimeValueException(subject, startTime, endTime);
        }
        if (endTime.isBefore(startTime)) {
            throw new InvalidTimeRangeException(subject, startTime, endTime);
        }
    }

    /**
     * Checks whether the given interval is valid as a <b>strict closed range</b>.
     *
     * <p>A strict closed range is valid only if:</p>
     * <ul>
     *   <li>{@code startTime} is non-null, and</li>
     *   <li>{@code endTime} is non-null, and</li>
     *   <li>{@code endTime} is strictly after {@code startTime} (equality is rejected).</li>
     * </ul>
     *
     * <h3>Examples</h3>
     * <ul>
     *   <li>{@code 10:00 – 10:00} is invalid</li>
     *   <li>{@code 10:00 – 10:01} is valid</li>
     * </ul>
     */
    public static boolean isValidStrictClosedRange(Instant startTime, Instant endTime) {
        return startTime != null && endTime != null && endTime.isAfter(startTime);
    }

    /**
     * Enforces that the given interval is valid as a <b>strict closed range</b>.
     *
     * <p>A strict closed range requires both timestamps to be present and {@code endTime} must be strictly
     * after {@code startTime}. Equality is rejected ({@code endTime == startTime}).</p>
     *
     * <p>Throws:</p>
     * <ul>
     *   <li>{@link MissingTimeValueException} if {@code startTime} or {@code endTime} is {@code null}</li>
     *   <li>{@link InvalidTimeRangeException} if both values are present but {@code endTime} is not after {@code startTime}
     *       (i.e., before or equal)</li>
     * </ul>
     *
     * <h3>Examples</h3>
     * <ul>
     *   <li>{@code 10:00 – 10:00} is invalid</li>
     *   <li>{@code 10:00 – 10:01} is valid</li>
     * </ul>
     *
     * @param subject   the domain subject (e.g., WORK_REPORT) for error reporting
     * @param startTime the start time (must not be {@code null})
     * @param endTime   the end time (must not be {@code null})
     * @throws MissingTimeValueException if {@code startTime} or {@code endTime} is {@code null}
     * @throws InvalidTimeRangeException if both values are present and {@code endTime} is not after {@code startTime}
     */
    public static void requireValidStrictClosedRange(Subject subject, Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new MissingTimeValueException(subject, startTime, endTime);
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidTimeRangeException(subject, startTime, endTime);
        }
    }
}