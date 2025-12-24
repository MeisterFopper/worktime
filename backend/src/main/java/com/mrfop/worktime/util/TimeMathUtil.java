package com.mrfop.worktime.util;

import java.time.Duration;
import java.time.Instant;

/**
 * Time arithmetic helpers (null-safe).
 *
 * <p>These methods intentionally do not throw domain exceptions and do not perform range validation.
 * They are safe to use for display and aggregation.</p>
 */
public final class TimeMathUtil {

    /**
     * Prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private TimeMathUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns the difference in seconds between {@code start} and {@code end}.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>If {@code start == null} or {@code end == null}: returns {@code 0}.</li>
     *   <li>If {@code end} is before {@code start}: returns {@code 0} (clamped).</li>
     *   <li>Otherwise: returns {@code Duration.between(start, end).getSeconds()}.</li>
     * </ul>
     */
    public static long diffSeconds(Instant start, Instant end) {
        if (start == null || end == null) return 0;
        long secs = Duration.between(start, end).getSeconds();
        return Math.max(0, secs);
    }
}