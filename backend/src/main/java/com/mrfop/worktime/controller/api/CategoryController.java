package com.mrfop.worktime.controller.api;

import com.mrfop.worktime.model.enums.ActiveStatus;
import com.mrfop.worktime.model.request.CategoryCreateRequest;
import com.mrfop.worktime.model.request.CategoryPatchRequest;
import com.mrfop.worktime.model.response.CategoryResponse;
import com.mrfop.worktime.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/categories")
@Tag(name = "1. Category Management API", description = "Manage category for time tracking")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestProblem"),
    @ApiResponse(responseCode = "409", ref = "#/components/responses/ConflictProblem"),
    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalErrorProblem")
})
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get categories by status")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestParam(defaultValue = "ALL") ActiveStatus status
    ) {
        return ResponseEntity.ok(categoryService.findByStatus(status));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<CategoryResponse> createCategory(
                @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{categoryId}")
    @Operation(summary = "Patch existing category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundProblem")
    })
    public ResponseEntity<CategoryResponse> patchCategory(
            @PathVariable @Min(1) Long categoryId,
            @Valid @RequestBody CategoryPatchRequest request
    ) {
        return ResponseEntity.ok(categoryService.patch(categoryId, request));
    }
}