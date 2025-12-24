package com.mrfop.worktime.util;

import com.mrfop.worktime.exception.InvalidParameterException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Strict request parsing helpers for common wire-level primitives.
 *
 * <p>This utility centralizes parsing of request parameters / DTO fields that are transmitted as
 * {@link String}s (e.g., JSON body fields). It intentionally performs <b>no implicit defaults</b>
 * (no "magic" fallbacks). If required information is missing or invalid, it fails fast by throwing
 * {@link InvalidParameterException} so the API response remains consistent via the global exception handler.</p>
 *
 * <h2>Error behavior</h2>
 * <ul>
 *   <li>Blank or {@code null} input is rejected for all {@code *Required(...)} methods.</li>
 *   <li>Parsing/format errors are wrapped into {@link InvalidParameterException} with the corresponding
 *       {@link Subject} and {@link LookupField} attached for structured error responses.</li>
 *   <li>Inputs are {@link String#trim() trimmed} prior to parsing.</li>
 * </ul>
 */
public final class ParsingUtil {

    /**
     * Prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private ParsingUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Parses a required ISO-8601 instant (UTC) from the request.
     *
     * <p>Expected format is compatible with {@link Instant#parse(CharSequence)} (e.g. {@code 2025-12-23T10:15:30Z}).</p>
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>Rejects {@code null} / blank input.</li>
     *   <li>Trims the input.</li>
     *   <li>Throws {@link InvalidParameterException} on parse failure.</li>
     * </ul>
     *
     * @param iso     the ISO instant string (required)
     * @param subject the domain subject for error reporting
     * @param field   the request field identifier for error reporting
     * @return parsed {@link Instant}
     * @throws InvalidParameterException if the value is missing/blank or not a valid ISO-8601 instant
     */
    public static Instant parseInstantIsoRequired(String iso, Subject subject, LookupField field) {
        try {
            if (iso == null || iso.isBlank()) {
                throw new InvalidParameterException(subject, field, iso);
            }
            return Instant.parse(iso.trim());
        } catch (InvalidParameterException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidParameterException(subject, field, iso, e);
        }
    }

    /**
     * Parses a required IANA time zone ID from the request.
     *
     * <p>Examples: {@code Europe/Berlin}, {@code UTC}.</p>
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>Rejects {@code null} / blank input.</li>
     *   <li>Trims the input.</li>
     *   <li>Throws {@link InvalidParameterException} if the time zone is unknown/invalid.</li>
     * </ul>
     *
     * @param tz      the time zone ID (required)
     * @param subject the domain subject for error reporting
     * @param field   the request field identifier for error reporting
     * @return parsed {@link ZoneId}
     * @throws InvalidParameterException if the value is missing/blank or not a valid time zone ID
     */
    public static ZoneId parseZoneRequired(String tz, Subject subject, LookupField field) {
        try {
            if (tz == null || tz.isBlank()) {
                throw new InvalidParameterException(subject, field, tz);
            }
            return ZoneId.of(tz.trim());
        } catch (InvalidParameterException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidParameterException(subject, field, tz, e);
        }
    }

    /**
     * Parses a required BCP-47 language tag from the request into a {@link Locale}.
     *
     * <p>Examples: {@code de-DE}, {@code en-US}, {@code de}.</p>
     *
     * <p><b>Important:</b> {@link Locale#forLanguageTag(String)} is permissive and does not throw for unknown tags.
     * To avoid silently accepting invalid values, this method performs a strict check that the resulting locale has
     * a non-blank language.</p>
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>Rejects {@code null} / blank input.</li>
     *   <li>Trims the input.</li>
     *   <li>Rejects tags that result in an empty language (e.g., malformed/unsupported tags).</li>
     * </ul>
     *
     * @param tag     the BCP-47 language tag (required)
     * @param subject the domain subject for error reporting
     * @param field   the request field identifier for error reporting
     * @return parsed {@link Locale}
     * @throws InvalidParameterException if the value is missing/blank or not a valid/usable BCP-47 language tag
     */
    public static Locale parseLocaleRequired(String tag, Subject subject, LookupField field) {
        if (tag == null || tag.isBlank()) {
            throw new InvalidParameterException(subject, field, tag);
        }

        Locale loc = Locale.forLanguageTag(tag.trim());

        if (loc.getLanguage() == null || loc.getLanguage().isBlank()) {
            throw new InvalidParameterException(subject, field, tag);
        }

        return loc;
    }
}