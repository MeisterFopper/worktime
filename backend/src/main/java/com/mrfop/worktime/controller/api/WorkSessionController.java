package com.mrfop.worktime.controller.api;

import com.mrfop.worktime.model.request.WorkSessionPatchRequest;
import com.mrfop.worktime.model.response.WorkSessionResponse;
import com.mrfop.worktime.service.WorkSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/worksessions")
@Tag(name = "3. Work Session API", description = "Manage work sessions")
@RequiredArgsConstructor
public class WorkSessionController {

    private final WorkSessionService sessionService;

    /* ------------------------- READ ------------------------- */

    @GetMapping
    @Operation(summary = "List work sessions (newest first)")
    public ResponseEntity<List<WorkSessionResponse>> list() {
        return ResponseEntity.ok(sessionService.findAll());
    }

    @GetMapping("/current")
    @Operation(summary = "Get currently active work session")
    public ResponseEntity<WorkSessionResponse> current() {
        return sessionService.current()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); // 204 if none
    }

    /* ------------------------- ACTIONS ------------------------- */

    @PostMapping("/start")
    @Operation(summary = "Start new work session (server time)")
    public ResponseEntity<WorkSessionResponse> start() {
        WorkSessionResponse started = sessionService.start();
        return ResponseEntity.status(HttpStatus.CREATED).body(started);
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop the currently running work session (server time)")
    public ResponseEntity<WorkSessionResponse> stop() {
        return ResponseEntity.ok(sessionService.stop());
    }

    /* ------------------------- ADMIN / EDITING ------------------------- */

    @PatchMapping("/{workSessionId}")
    @Operation(summary = "Patch a work session entry (optional admin)")
    public ResponseEntity<WorkSessionResponse> patch(
            @PathVariable Long workSessionId,
            @Valid @RequestBody WorkSessionPatchRequest request
    ) {
        return ResponseEntity.ok(sessionService.patch(workSessionId, request));
    }

    @DeleteMapping("/{workSessionId}")
    @Operation(summary = "Delete a work session entry (optional admin)")
    public ResponseEntity<Void> delete(@PathVariable Long workSessionId) {
        sessionService.delete(workSessionId);
        return ResponseEntity.noContent().build();
    }
}