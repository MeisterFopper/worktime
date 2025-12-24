package com.mrfop.worktime.exception;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.ExceptionMessages;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;

public final class NameAlreadyExistsException extends DomainException {

    private final LookupField field = LookupField.NAME;
    private final Object value;

    public NameAlreadyExistsException(Subject subject, String name) {
        super(ErrorCode.NAME_ALREADY_EXISTS, subject,
              ExceptionMessages.alreadyExists(subject, LookupField.NAME, name));
        this.value = name;
    }

    public NameAlreadyExistsException(Subject subject, String name, Throwable cause) {
        super(ErrorCode.NAME_ALREADY_EXISTS, subject,
              ExceptionMessages.alreadyExists(subject, LookupField.NAME, name), cause);
        this.value = name;
    }

    public LookupField getField() { return field; }
    public Object getValue() { return value; }
}