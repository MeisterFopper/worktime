package com.mrfop.worktime.exception;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.ExceptionMessages;
import com.mrfop.worktime.exception.base.Subject;

public final class NoFieldsToUpdateException extends DomainException {
    public NoFieldsToUpdateException(Subject subject) {
        super(ErrorCode.NO_FIELDS_TO_UPDATE, subject, ExceptionMessages.noFieldsToUpdate(subject));
    }
}