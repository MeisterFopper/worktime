package com.mrfop.worktime.persistence;

import com.mrfop.worktime.exception.NotFoundException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.entity.WorkSegmentEntity;
import com.mrfop.worktime.model.mapper.WorkSegmentMapper;
import com.mrfop.worktime.model.request.WorkSegmentPatchRequest;
import com.mrfop.worktime.model.request.WorkSegmentStartRequest;
import com.mrfop.worktime.model.response.WorkSegmentResponse;
import com.mrfop.worktime.repository.WorkSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkSegmentPersistence {

    private final WorkSegmentRepository repo;
    private final WorkSegmentMapper mapper;

    /* ------------------------- READ (mapped DTOs) ------------------------- */

    public List<WorkSegmentResponse> findAll() {
        return repo.findAllByOrderByStartTimeDesc().stream().map(mapper::toResponse).toList();
    }

    public Optional<WorkSegmentResponse> findCurrent() {
        return repo.findTopByEndTimeIsNullOrderByStartTimeDesc().map(mapper::toResponse);
    }

    /* ------------------------- DB primitives (locking / entities) ------------------------- */

    public Optional<WorkSegmentEntity> lockCurrentForUpdate() {
        return repo.lockCurrentForUpdate();
    }

    public WorkSegmentEntity lockByIdForUpdate(Long id) {
        return repo.lockByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException(Subject.WORK_SEGMENT, LookupField.ID, id));
    }

    /* ------------------------- Mapping helpers (no DB write) ------------------------- */

    public WorkSegmentEntity toNewEntity(WorkSegmentStartRequest request) {
        return mapper.toNewEntity(request);
    }

    public void applyPatch(WorkSegmentEntity entity, WorkSegmentPatchRequest request) {
        mapper.applyPatch(request, entity);
    }

    /* ------------------------- Writes ------------------------- */

    public WorkSegmentResponse saveAndFlush(WorkSegmentEntity entity) {
        WorkSegmentEntity saved = repo.save(entity);
        repo.flush();
        return mapper.toResponse(saved);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}