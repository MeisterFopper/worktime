package com.mrfop.worktime.controller;

import com.mrfop.worktime.exception.InvalidParameterException;
import com.mrfop.worktime.exception.InvalidTimeRangeException;
import com.mrfop.worktime.exception.MissingTimeValueException;
import com.mrfop.worktime.exception.NameAlreadyExistsException;
import com.mrfop.worktime.exception.NotFoundException;
import com.mrfop.worktime.exception.ValidationException;
import com.mrfop.worktime.exception.base.DomainException;
import com.mrfop.worktime.exception.base.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<ErrorCode, HttpStatus> CODE_TO_STATUS = new EnumMap<>(ErrorCode.class);

    static {
        // 400
        CODE_TO_STATUS.put(ErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST);
        CODE_TO_STATUS.put(ErrorCode.NO_FIELDS_TO_UPDATE, HttpStatus.BAD_REQUEST);
        CODE_TO_STATUS.put(ErrorCode.INVALID_TIME_RANGE, HttpStatus.BAD_REQUEST);
        CODE_TO_STATUS.put(ErrorCode.MISSING_TIME_VALUE, HttpStatus.BAD_REQUEST);

        // 404
        CODE_TO_STATUS.put(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND);

        // 409
        CODE_TO_STATUS.put(ErrorCode.CONFLICT, HttpStatus.CONFLICT);
        CODE_TO_STATUS.put(ErrorCode.NAME_ALREADY_EXISTS, HttpStatus.CONFLICT);
        CODE_TO_STATUS.put(ErrorCode.ALREADY_RUNNING, HttpStatus.CONFLICT);
        CODE_TO_STATUS.put(ErrorCode.NO_ACTIVE, HttpStatus.CONFLICT);
        CODE_TO_STATUS.put(ErrorCode.OPERATION_BLOCKED_BY_RUNNING, HttpStatus.CONFLICT);
    }

    /* ------------------------- DOMAIN EXCEPTIONS ------------------------- */

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(DomainException ex, HttpServletRequest req) {
        HttpStatus status = CODE_TO_STATUS.getOrDefault(ex.getCode(), HttpStatus.BAD_REQUEST);

        ProblemDetail pd = base(status, ex.getMessage(), problemType(ex.getCode()), req);
        pd.setProperty("code", ex.getCode().name());
        pd.setProperty("subject", ex.getResource() == null ? null : ex.getResource().name());

        if (ex instanceof ValidationException vex) {
            if (vex.getField() != null) pd.setProperty("field", vex.getField().wireName());
            if (vex.getValue() != null) pd.setProperty("value", vex.getValue());
        }

        if (ex instanceof NotFoundException nfx) {
            if (nfx.getField() != null) pd.setProperty("field", nfx.getField().wireName());
            if (nfx.getValue() != null) pd.setProperty("value", nfx.getValue());
        }

        if (ex instanceof NameAlreadyExistsException nax) {
            if (nax.getField() != null) pd.setProperty("field", nax.getField().wireName());
            if (nax.getValue() != null) pd.setProperty("value", nax.getValue());
        }

        if (ex instanceof InvalidParameterException ipx) {
            if (ipx.getField() != null) pd.setProperty("field", ipx.getField().wireName());
            if (ipx.getValue() != null) pd.setProperty("value", ipx.getValue());
        }

        if (ex instanceof InvalidTimeRangeException itx) {
            pd.setProperty("start", itx.getStart());
            pd.setProperty("end", itx.getEnd());
        }

        if (ex instanceof MissingTimeValueException mtx) {
            pd.setProperty("start", mtx.getStart());
            pd.setProperty("end", mtx.getEnd());
        }

        logAtLevel(status, ex, pd.getDetail());
        return ResponseEntity.status(status).body(pd);
    }

    /* ------------------------- 400: INPUT / VALIDATION ------------------------- */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProblemDetail pd = base(status, "Validation failed", problemType(ErrorCode.VALIDATION_FAILED), req);
        pd.setProperty("code", ErrorCode.VALIDATION_FAILED.name());

        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        pd.setProperty("errors", errors);

        logAtLevel(status, ex, pd.getDetail());
        return ResponseEntity.status(status).body(pd);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String msg = "Invalid value for parameter '" + ex.getName() + "'. Expected type: " + expected;

        ProblemDetail pd = base(status, msg, problemType(ErrorCode.VALIDATION_FAILED), req);
        pd.setProperty("code", ErrorCode.VALIDATION_FAILED.name());
        pd.setProperty("param", ex.getName());

        logAtLevel(status, ex, pd.getDetail());
        return ResponseEntity.status(status).body(pd);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProblemDetail pd = base(
                status,
                "Missing required parameter '" + ex.getParameterName() + "'",
                problemType(ErrorCode.VALIDATION_FAILED),
                req
        );
        pd.setProperty("code", ErrorCode.VALIDATION_FAILED.name());

        logAtLevel(status, ex, pd.getDetail());
        return ResponseEntity.status(status).body(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProblemDetail pd = base(status, "Malformed or unreadable request body", problemType(ErrorCode.VALIDATION_FAILED), req);
        pd.setProperty("code", ErrorCode.VALIDATION_FAILED.name());

        logAtLevel(status, ex, pd.getDetail());
        return ResponseEntity.status(status).body(pd);
    }

    /* ------------------------- 405 ------------------------- */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;

        ProblemDetail pd = base(status, "Method not allowed", URI.create("about:blank"), req);
        pd.setProperty("code", "METHOD_NOT_ALLOWED");

        logAtLevel(status, ex, pd.getDetail());
        return ResponseEntity.status(status).body(pd);
    }

    /* ------------------------- ResponseStatusException + legacy @ResponseStatus support ------------------------- */

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String detail = (ex.getReason() != null && !ex.getReason().isBlank())
                ? ex.getReason()
                : safeMessage(ex);

        ProblemDetail pd = base(status, detail, URI.create("about:blank"), req);
        pd.setProperty("code", "HTTP_" + status.value());

        logAtLevel(status, ex, pd.getDetail());
        return ResponseEntity.status(status).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAny(Exception ex, HttpServletRequest req) {
        HttpStatus status = resolveHttpStatusFromAnnotations(ex).orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        String detail = status.is5xxServerError()
                ? "Unexpected internal error occurred"
                : safeMessage(ex);

        ProblemDetail pd = base(status, detail, URI.create("about:blank"), req);
        pd.setProperty("code", "INTERNAL_ERROR");

        logAtLevel(status, ex, pd.getDetail());
        return ResponseEntity.status(status).body(pd);
    }

    /* ------------------------- helpers ------------------------- */

    private ProblemDetail base(HttpStatus status, String detail, URI type, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setType(type == null ? URI.create("about:blank") : type);
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    private URI problemType(ErrorCode code) {
        if (code == null) return URI.create("about:blank");
        return URI.create("https://api.mrfop.com/problems/" + code.name());
    }

    private Map<String, Object> toFieldError(FieldError fe) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("field", fe.getField());
        m.put("message", fe.getDefaultMessage());
        return m;
    }

    private void logAtLevel(HttpStatus status, Exception ex, String detail) {
        if (status.is5xxServerError()) {
            log.error("Request failed ({}): {}", status.value(), detail, ex);
        } else {
            log.warn("Request failed ({}): {}", status.value(), detail);
        }
    }

    private String safeMessage(Throwable ex) {
        if (ex.getMessage() == null || ex.getMessage().isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return ex.getMessage();
    }

    private java.util.Optional<HttpStatus> resolveHttpStatusFromAnnotations(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            ResponseStatus rs = AnnotatedElementUtils.findMergedAnnotation(cur.getClass(), ResponseStatus.class);
            if (rs != null) {
                HttpStatus status = rs.code();
                if (status == HttpStatus.INTERNAL_SERVER_ERROR && rs.value() != HttpStatus.INTERNAL_SERVER_ERROR) {
                    status = rs.value();
                }
                return java.util.Optional.of(status);
            }
            cur = cur.getCause();
        }
        return java.util.Optional.empty();
    }
}