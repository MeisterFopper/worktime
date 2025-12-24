package com.mrfop.worktime.exception;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.ExceptionMessages;
import com.mrfop.worktime.exception.base.Subject;

public class ConflictException extends DomainException {

    public ConflictException(Subject subject) {
        super(ErrorCode.CONFLICT, subject, ExceptionMessages.conflictsWithExistingData(subject));
    }

    public ConflictException(Subject subject, Throwable cause) {
        super(ErrorCode.CONFLICT, subject, ExceptionMessages.conflictsWithExistingData(subject), cause);
    }

    public ConflictException(Subject subject, String message) {
        super(ErrorCode.CONFLICT, subject, message);
    }

    public ConflictException(Subject subject, String message, Throwable cause) {
        super(ErrorCode.CONFLICT, subject, message, cause);
    }
}