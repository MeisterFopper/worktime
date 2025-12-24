package com.mrfop.worktime.persistence;

import com.mrfop.worktime.exception.NotFoundException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.entity.WorkSessionEntity;
import com.mrfop.worktime.model.mapper.WorkSessionMapper;
import com.mrfop.worktime.model.request.WorkSessionPatchRequest;
import com.mrfop.worktime.model.response.WorkSessionResponse;
import com.mrfop.worktime.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkSessionPersistence {

    private final WorkSessionRepository repo;
    private final WorkSessionMapper mapper;

    /* ------------------------- READ (mapped DTOs) ------------------------- */

    public List<WorkSessionResponse> findAll() {
        return repo.findAllByOrderByStartTimeDesc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Optional<WorkSessionResponse> findCurrent() {
        return repo.findTopByEndTimeIsNullOrderByStartTimeDesc().map(mapper::toResponse);
    }

    public List<WorkSessionResponse> findOverlappingWithSegments(Instant nowUtc, Instant fromInclusive, Instant toExclusive) {
        return repo.findOverlappingWithSegments(nowUtc, fromInclusive, toExclusive)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /* ------------------------- DB primitives ------------------------- */

    public Optional<WorkSessionEntity> lockCurrentForUpdate() {
        return repo.lockCurrentForUpdate();
    }

    public WorkSessionEntity lockByIdForUpdate(Long id) {
        return repo.lockByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException(Subject.WORK_SESSION, LookupField.ID, id));
    }

    /* ------------------------- Mapping helpers (no DB write) ------------------------- */

    public void applyPatch(WorkSessionEntity entity, WorkSessionPatchRequest request) {
        mapper.applyPatch(request, entity);
    }

    /* ------------------------- Writes ------------------------- */

    public WorkSessionResponse saveAndFlush(WorkSessionEntity entity) {
        WorkSessionEntity saved = repo.save(entity);
        repo.flush();
        return mapper.toResponse(saved);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}