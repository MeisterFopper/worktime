package com.mrfop.worktime.exception;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.Subject;

public final class NoActiveException extends DomainException {

    public NoActiveException(Subject subject) {
        super(ErrorCode.NO_ACTIVE, subject, "No active " + subject + " found.");
    }

    public NoActiveException(Subject subject, Throwable cause) {
        super(ErrorCode.NO_ACTIVE, subject, "No active " + subject + " found.", cause);
    }
}