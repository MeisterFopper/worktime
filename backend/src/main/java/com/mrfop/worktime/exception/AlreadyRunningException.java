package com.mrfop.worktime.exception;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.ExceptionMessages;
import com.mrfop.worktime.exception.base.Subject;

public final class AlreadyRunningException extends DomainException {

    public AlreadyRunningException(Subject subject) {
        super(ErrorCode.ALREADY_RUNNING, subject, ExceptionMessages.alreadyRunning(subject));
    }

    public AlreadyRunningException(Subject subject, Throwable cause) {
        super(ErrorCode.ALREADY_RUNNING, subject, ExceptionMessages.alreadyRunning(subject), cause);
    }
}