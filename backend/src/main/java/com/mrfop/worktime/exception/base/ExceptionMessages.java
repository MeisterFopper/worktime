package com.mrfop.worktime.exception.base;

import java.time.Instant;

public final class ExceptionMessages {
    private ExceptionMessages() {}

    public static String alreadyExists(Subject subject, LookupField field, Object value) {
        return capitalize(subject.label()) + " " + field.wireName() + " already exists: " + value;
    }

    public static String alreadyRunning(Subject subject) {
        return capitalize(subject.label()) + " is already running.";
    }

    public static String cannotBeBlank(Subject subject, LookupField field) {
        return capitalize(subject.label()) + " " + field.wireName() + " cannot be blank.";
    }

    public static String operationBlockedByRunning(Subject target, Subject running) {
        return capitalize(target.label()) + " is not allowed while " + running.label() + " is running.";
    }

    public static String conflictsWithExistingData(Subject subject) {
        return capitalize(subject.label()) + " conflicts with existing data.";
    }

    public static String notFound(Subject subject) {
        return capitalize(subject.label()) + " not found.";
    }

    public static String notFound(Subject subject, LookupField field, Object value) {
        if (value == null) return notFound(subject);
        return capitalize(subject.label()) + " not found: " + field.wireName() + "=" + value;
    }

    public static String invalidParameter(Subject subject, LookupField field, Object value) {
        return capitalize(subject.label()) + " parameter '" + field.wireName() + "' has invalid value: " + value;
    }


    public static String invalidTimeRange(Instant start, Instant end) {
        return "Invalid time range: end time (" + end + ") must be after start time (" + start + ").";
    }

    public static String missingTimeValue(Subject subject, boolean missingStart, boolean missingEnd) {
        if (missingStart && missingEnd) {
            return capitalize(subject.label()) + " time range requires both start time and end time.";
        }
        if (missingStart) {
            return capitalize(subject.label()) + " time range requires start time.";
        }
        return capitalize(subject.label()) + " time range requires end time.";
    }

    public static String noFieldsToUpdate(Subject subject) {
        return "No fields to update for " + subject.label() + ".";
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}