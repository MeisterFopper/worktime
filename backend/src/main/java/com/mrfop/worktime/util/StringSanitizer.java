package com.mrfop.worktime.util;

/**
 * Utility class for simple, consistent string sanitization.
 * <p>
 * The primary goal is to centralize common input normalization rules (currently trimming)
 * so validation and persistence layers behave consistently.
 */
public final class StringSanitizer {

    /**
     * Prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private StringSanitizer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Normalizes a string by trimming leading and trailing whitespace.
     * <p>
     * If the input is {@code null}, {@code null} is returned.
     *
     * @param raw the raw input string (may be {@code null})
     * @return the trimmed string, or {@code null} if {@code raw} was {@code null}
     */
    public static String normalize(String raw) {
        return raw == null ? null : raw.trim();
    }

    /**
     * Normalizes a string by trimming leading and trailing whitespace and guarantees a non-null result.
     * <p>
     * If the input is {@code null}, an empty string ({@code ""}) is returned.
     *
     * @param raw the raw input string (may be {@code null})
     * @return the trimmed string, or {@code ""} if {@code raw} was {@code null}
     */
    public static String normalizeNonNull(String raw) {
        String s = normalize(raw);
        return s == null ? "" : s;
    }
}