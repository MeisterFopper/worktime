package com.mrfop.worktime.exception;

import java.time.Instant;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.ExceptionMessages;
import com.mrfop.worktime.exception.base.Subject;

public final class MissingTimeValueException extends DomainException {

    private final Instant start;
    private final Instant end;

    public MissingTimeValueException(Subject subject, Instant start, Instant end) {
        super(
            ErrorCode.MISSING_TIME_VALUE,
            subject,
            ExceptionMessages.missingTimeValue(subject, start == null, end == null)
        );
        this.start = start;
        this.end = end;
    }

    public MissingTimeValueException(Subject subject, Instant start, Instant end, Throwable cause) {
        super(
            ErrorCode.MISSING_TIME_VALUE,
            subject,
            ExceptionMessages.missingTimeValue(subject, start == null, end == null),
            cause
        );
        this.start = start;
        this.end = end;
    }

    public Instant getStart() { return start; }
    public Instant getEnd() { return end; }
}