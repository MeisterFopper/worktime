package com.mrfop.worktime.exception;

import com.mrfop.worktime.exception.base.*;

public final class ValidationException extends DomainException {

    private final LookupField field;
    private final Object value;

    public ValidationException(Subject subject, LookupField field, Object value) {
        super(ErrorCode.VALIDATION_FAILED, subject, ExceptionMessages.cannotBeBlank(subject, field));
        this.field = field;
        this.value = value;
    }

    public LookupField getField() { return field; }
    public Object getValue() { return value; }
}