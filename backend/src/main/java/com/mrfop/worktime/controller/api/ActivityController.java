package com.mrfop.worktime.controller.api;

import com.mrfop.worktime.model.enums.ActiveStatus;
import com.mrfop.worktime.model.request.ActivityCreateRequest;
import com.mrfop.worktime.model.request.ActivityPatchRequest;
import com.mrfop.worktime.model.response.ActivityResponse;
import com.mrfop.worktime.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/activities")
@Tag(name = "2. Activity Management API", description = "Manage activity for time tracking")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestProblem"),
    @ApiResponse(responseCode = "409", ref = "#/components/responses/ConflictProblem"),
    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalErrorProblem")
})
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @Operation(summary = "Get activities by status")
    public ResponseEntity<List<ActivityResponse>> getActivities(
            @RequestParam(defaultValue = "ALL") ActiveStatus status
    ) {
        return ResponseEntity.ok(activityService.findByStatus(status));
    }

    @PostMapping
    @Operation(summary = "Create a new activity")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<ActivityResponse> createActivity(
                @Valid @RequestBody ActivityCreateRequest request
    ) {
        ActivityResponse created = activityService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{activityId}")
    @Operation(summary = "Patch existing activity")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundProblem")
    })
    public ResponseEntity<ActivityResponse> patchActivity(
                @PathVariable @Min(1) Long activityId,
                @Valid @RequestBody ActivityPatchRequest request
    ) {
        return ResponseEntity.ok(activityService.patch(activityId, request));
    }
}