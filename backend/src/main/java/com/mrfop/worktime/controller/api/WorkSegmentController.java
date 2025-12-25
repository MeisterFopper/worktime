package com.mrfop.worktime.controller.api;

import com.mrfop.worktime.model.request.WorkSegmentPatchRequest;
import com.mrfop.worktime.model.request.WorkSegmentStartRequest;
import com.mrfop.worktime.model.request.WorkSegmentStopRequest;
import com.mrfop.worktime.model.response.WorkSegmentResponse;
import com.mrfop.worktime.service.WorkSegmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/worksegments")
@Tag(name = "4. Work Segment API", description = "Manage work segment entries")
@RequiredArgsConstructor
public class WorkSegmentController {

    private final WorkSegmentService segmentService;

    /* ------------------------- READ ------------------------- */

    @GetMapping
    @Operation(summary = "List work sessions (newest first)")
    public ResponseEntity<List<WorkSegmentResponse>> list() {
        return ResponseEntity.ok(segmentService.findAll());
    }

    @GetMapping("/current")
    @Operation(summary = "Get currently active work segment")
    public ResponseEntity<WorkSegmentResponse> getCurrent() {
        return segmentService.current()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); // 204 if none
    }

    /* ------------------------- ACTIONS ------------------------- */

    @PostMapping("/start")
    @Operation(summary = "Start a new work segment")
    public ResponseEntity<WorkSegmentResponse> start(@Valid @RequestBody WorkSegmentStartRequest request) {
        WorkSegmentResponse created = segmentService.start(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop the currently running work segment")
    public ResponseEntity<WorkSegmentResponse> stop(@Valid @RequestBody WorkSegmentStopRequest request) {
        return ResponseEntity.ok(segmentService.stop(request));
    }

    /* ------------------------- ADMIN / EDITING ------------------------- */

    @PatchMapping("/{workSegmentId}")
    @Operation(summary = "Patch a work segment entry")
    public ResponseEntity<WorkSegmentResponse> patch(
            @PathVariable Long workSegmentId,
            @Valid @RequestBody WorkSegmentPatchRequest request
    ) {
        return ResponseEntity.ok(segmentService.patch(workSegmentId, request));
    }

    @DeleteMapping("/{workSegmentId}")
    @Operation(summary = "Delete a work segment entry")
    public ResponseEntity<Void> delete(@PathVariable Long workSegmentId) {
        segmentService.delete(workSegmentId);
        return ResponseEntity.noContent().build();
    }
}