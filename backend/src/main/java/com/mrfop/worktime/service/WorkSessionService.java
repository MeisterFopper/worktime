package com.mrfop.worktime.service;

import com.mrfop.worktime.exception.AlreadyRunningException;
import com.mrfop.worktime.exception.OperationBlockedByRunningException;
import com.mrfop.worktime.exception.ConflictException;
import com.mrfop.worktime.exception.NoActiveException;
import com.mrfop.worktime.exception.NoFieldsToUpdateException;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.entity.WorkSessionEntity;
import com.mrfop.worktime.model.request.WorkSessionPatchRequest;
import com.mrfop.worktime.model.response.WorkSessionResponse;
import com.mrfop.worktime.persistence.WorkSegmentPersistence;
import com.mrfop.worktime.persistence.WorkSessionPersistence;
import com.mrfop.worktime.util.TimeRangeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkSessionService {

    private final WorkSessionPersistence persistence;
    private final WorkSegmentPersistence workSegmentPersistence;

    /* ------------------------- READ ------------------------- */

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "workSessions", key = "'all'")
    public List<WorkSessionResponse> findAll() {
        return persistence.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "workSessions", key = "'current'")
    public Optional<WorkSessionResponse> current() {
        return persistence.findCurrent();
    }

    @Transactional(readOnly = true)
    public boolean isRunning() {
        return current().isPresent();
    }

    /* ------------------------- ACTIONS ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = { "workSessions", "workSegments" }, allEntries = true)
    public WorkSessionResponse start() {
        ensureNoActiveSession();

        WorkSessionEntity session = new WorkSessionEntity();
        session.setStartTime(now());
        session.setEndTime(null);

        return saveOrThrow(session);
    }

    @Transactional
    @CacheEvict(cacheNames = { "workSessions", "workSegments" }, allEntries = true)
    public WorkSessionResponse stop() {
        WorkSessionEntity current = lockCurrentSessionOrThrow();

        ensureNoActiveSegment();

        current.setEndTime(now());
        validateRangeOrThrow(current.getStartTime(), current.getEndTime());

        return saveOrThrow(current);
    }

    /* ------------------------- PATCH ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = { "workSessions", "workSegments" }, allEntries = true)
    public WorkSessionResponse patch(Long id, WorkSessionPatchRequest request) {
        if (!request.hasAny()) throw new NoFieldsToUpdateException(Subject.WORK_SESSION);

        // Request-level sanity check
        validateRangeOrThrow(request.startTime(), request.endTime());

        WorkSessionEntity session = persistence.lockByIdForUpdate(id);

        // Apply patch in-memory (no DB write yet)
        persistence.applyPatch(session, request);

        // Validate final entity state BEFORE saving/flushing
        validateRangeOrThrow(session.getStartTime(), session.getEndTime());

        // Now persist
        return saveOrThrow(session);
    }

    /* ------------------------- DELETE ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = { "workSessions", "workSegments" }, allEntries = true)
    public void delete(Long workSessionId) {
        persistence.deleteById(workSessionId);
    }

    /* ------------------------- HELPERS ------------------------- */

    /* ------------------------- HELPERS: TIME ------------------------- */

    private Instant now() {
        return Instant.now();
    }

    /* ------------------------- HELPERS: GUARDS / RESOLVERS ------------------------- */

    private void ensureNoActiveSession() {
        if (persistence.lockCurrentForUpdate().isPresent()) {
            throw new AlreadyRunningException(Subject.WORK_SESSION);
        }
    }

    private WorkSessionEntity lockCurrentSessionOrThrow() {
        return persistence.lockCurrentForUpdate()
                .orElseThrow(() -> new NoActiveException(Subject.WORK_SESSION));
    }

    private void ensureNoActiveSegment() {
        if (workSegmentPersistence.lockCurrentForUpdate().isPresent()) {
            throw new OperationBlockedByRunningException(Subject.WORK_SESSION, Subject.WORK_SEGMENT);
        }
    }

    /* ------------------------- HELPERS: VALIDATION ------------------------- */

    private void validateRangeOrThrow(Instant start, Instant end) {
        TimeRangeUtil.requireValidOpenRange(Subject.WORK_SESSION, start, end);
    }

    /* ------------------------- HELPERS: PERSISTENCE WRAPPERS ------------------------- */

    private WorkSessionResponse saveOrThrow(WorkSessionEntity entity) {
        try {
            return persistence.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(Subject.WORK_SESSION, e);
        }
    }
}