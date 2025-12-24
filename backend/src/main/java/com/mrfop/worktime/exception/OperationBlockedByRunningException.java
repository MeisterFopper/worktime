package com.mrfop.worktime.exception;

import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import com.mrfop.worktime.exception.base.ExceptionMessages;
import com.mrfop.worktime.exception.base.Subject;

public final class OperationBlockedByRunningException extends DomainException {

    public OperationBlockedByRunningException(Subject toStop, Subject running) {
        super(
            ErrorCode.OPERATION_BLOCKED_BY_RUNNING,
            Subject.WORK_SESSION,
            ExceptionMessages.operationBlockedByRunning(toStop, running)
        );
    }

    public OperationBlockedByRunningException(Subject toStop, Subject running, Throwable cause) {
        super(
            ErrorCode.OPERATION_BLOCKED_BY_RUNNING,
            Subject.WORK_SESSION,
            ExceptionMessages.operationBlockedByRunning(toStop, running),
            cause
        );
    }
}