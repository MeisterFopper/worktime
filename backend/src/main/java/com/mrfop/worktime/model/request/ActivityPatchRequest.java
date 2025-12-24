package com.mrfop.worktime.model.request;

import com.mrfop.worktime.validation.NullOrNotBlank;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record ActivityPatchRequest(
    @Size(max = 255) @NullOrNotBlank(message = "Activity name cannot be blank") String name,
    @Size(max = 500) String description,
    Boolean active
) {
    @AssertTrue(message = "Provide at least one field to patch")
    public boolean hasAny() {
        return name != null
                || description != null
                || active != null;
    }
}