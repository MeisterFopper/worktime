package com.mrfop.worktime.exception.base;

public abstract class DomainException extends RuntimeException {
    private final ErrorCode code;
    private final Subject resource;

    protected DomainException(ErrorCode code, Subject resource, String message) {
        super(message);
        this.code = code;
        this.resource = resource;
    }

    protected DomainException(ErrorCode code, Subject resource, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.resource = resource;
    }

    public ErrorCode getCode() { return code; }
    public Subject getResource() { return resource; }
}