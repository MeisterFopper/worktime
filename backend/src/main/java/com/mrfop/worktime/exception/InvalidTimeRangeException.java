package com.mrfop.worktime.exception;

import java.time.Instant;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.ExceptionMessages;
import com.mrfop.worktime.exception.base.Subject;

public final class InvalidTimeRangeException extends DomainException {

    private final Instant start;
    private final Instant end;

    public InvalidTimeRangeException(Subject subject, Instant start, Instant end) {
        super(ErrorCode.INVALID_TIME_RANGE, subject, ExceptionMessages.invalidTimeRange(start, end));
        this.start = start;
        this.end = end;
    }

    public InvalidTimeRangeException(Subject subject, Instant start, Instant end, Throwable cause) {
        super(ErrorCode.INVALID_TIME_RANGE, subject, ExceptionMessages.invalidTimeRange(start, end), cause);
        this.start = start;
        this.end = end;
    }

    public Instant getStart() { return start; }
    public Instant getEnd() { return end; }
}