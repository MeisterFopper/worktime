package com.mrfop.worktime.model.response;

import java.time.Instant;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {}