package com.mrfop.worktime.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(type = "object", description = "RFC 7807 Problem Details with application-specific extensions.")
public class ApiProblem {

    @Schema(example = "https://api.mrfop.com/problems/NOT_FOUND")
    public String type;

    @Schema(example = "Not Found")
    public String title;

    @Schema(example = "Category not found: id=123")
    public String detail;

    @Schema(example = "404")
    public Integer status;

    @Schema(example = "/api/v1/categories/123")
    public String instance;

    @Schema(example = "NOT_FOUND")
    public String code;

    @Schema(example = "CATEGORY")
    public String subject;

    @Schema(example = "id")
    public String field;

    @Schema(example = "VALUE123")
    public Object value;

    @Schema(example = "2025-12-17T10:15:30Z")
    public String timestamp;

    @Schema(description = "Field validation errors (when applicable)", nullable = true)
    public List<FieldViolation> errors;

    @Schema(type = "object", name = "FieldViolation")
    public static class FieldViolation {

        @Schema(example = "name")
        public String field;

        @Schema(example = "must not be blank")
        public String message;
    }
}