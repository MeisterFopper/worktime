package com.mrfop.worktime.exception;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.ExceptionMessages;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;

public final class NotFoundException extends DomainException {

    private final LookupField field;
    private final Object value;

    public NotFoundException(Subject subject) {
        super(ErrorCode.NOT_FOUND, subject, ExceptionMessages.notFound(subject));
        this.field = null;
        this.value = null;
    }

    public NotFoundException(Subject subject, LookupField field, Object value) {
        super(ErrorCode.NOT_FOUND, subject, ExceptionMessages.notFound(subject, field, value));
        this.field = field;
        this.value = value;
    }

    public NotFoundException(Subject subject, LookupField field, Object value, Throwable cause) {
        super(ErrorCode.NOT_FOUND, subject, ExceptionMessages.notFound(subject, field, value), cause);
        this.field = field;
        this.value = value;
    }

    public LookupField getField() { return field; }
    public Object getValue() { return value; }
}