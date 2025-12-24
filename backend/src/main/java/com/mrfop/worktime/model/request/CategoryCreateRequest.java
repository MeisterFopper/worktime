package com.mrfop.worktime.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
    @Size(max = 255) @NotBlank(message = "Category name cannot be blank") String name,
    @Size(max = 500) String description,
    boolean active
) {}