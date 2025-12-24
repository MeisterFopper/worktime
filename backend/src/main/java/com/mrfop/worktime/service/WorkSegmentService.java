package com.mrfop.worktime.service;

import com.mrfop.worktime.exception.AlreadyRunningException;
import com.mrfop.worktime.exception.ConflictException;
import com.mrfop.worktime.exception.NoActiveException;
import com.mrfop.worktime.exception.NoFieldsToUpdateException;
import com.mrfop.worktime.exception.NotFoundException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.entity.ActivityEntity;
import com.mrfop.worktime.model.entity.CategoryEntity;
import com.mrfop.worktime.model.entity.WorkSegmentEntity;
import com.mrfop.worktime.model.entity.WorkSessionEntity;
import com.mrfop.worktime.model.request.WorkSegmentPatchRequest;
import com.mrfop.worktime.model.request.WorkSegmentStartRequest;
import com.mrfop.worktime.model.request.WorkSegmentStopRequest;
import com.mrfop.worktime.model.response.WorkSegmentResponse;
import com.mrfop.worktime.persistence.WorkSegmentPersistence;
import com.mrfop.worktime.repository.ActivityRepository;
import com.mrfop.worktime.repository.CategoryRepository;
import com.mrfop.worktime.repository.WorkSessionRepository;
import com.mrfop.worktime.util.StringSanitizer;
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
public class WorkSegmentService {

    private final WorkSegmentPersistence persistence;

    // Data access used for orchestration (resolvers hide the details)
    private final WorkSessionRepository sessionRepo;
    private final CategoryRepository categoryRepo;
    private final ActivityRepository activityRepo;

    /* ------------------------- READ ------------------------- */

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "workSegments", key = "'all'")
    public List<WorkSegmentResponse> findAll() {
        return persistence.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "workSegments", key = "'current'")
    public Optional<WorkSegmentResponse> current() {
        return persistence.findCurrent();
    }

    @Transactional(readOnly = true)
    public boolean isRunning() {
        return current().isPresent();
    }

    /* ------------------------- ACTIONS ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = { "workSegments", "workSessions" }, allEntries = true)
    public WorkSegmentResponse start(WorkSegmentStartRequest request) {
        WorkSegmentStartRequest norm = normalizeStart(request);

        ensureNoOpenSegment();

        WorkSessionEntity session = resolveCurrentSessionOrThrow();
        CategoryEntity category = resolveCategoryOrThrow(norm.categoryId());
        ActivityEntity activity = resolveActivityOrThrow(norm.activityId());

        WorkSegmentEntity entity = persistence.toNewEntity(norm);
        entity.setWorkSession(session);
        entity.setCategory(category);
        entity.setActivity(activity);
        entity.setStartTime(now());
        entity.setEndTime(null);
        entity.setComment(norm.comment());

        return saveOrThrow(entity);
    }

    @Transactional
    @CacheEvict(cacheNames = { "workSegments", "workSessions" }, allEntries = true)
    public WorkSegmentResponse stop(WorkSegmentStopRequest request) {
        WorkSegmentEntity open = resolveCurrentSegmentOrThrow();

        open.setEndTime(now());
        applyStopUpdates(open, request);

        validateRange(open.getStartTime(), open.getEndTime());

        return saveOrThrow(open);
    }

    /* ------------------------- PATCH ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = { "workSegments", "workSessions" }, allEntries = true)
    public WorkSegmentResponse patch(Long workSegmentId, WorkSegmentPatchRequest request) {
        if (!request.hasAny()) throw new NoFieldsToUpdateException(Subject.WORK_SEGMENT);

        // Request-level sanity check
        validateRange(request.startTime(), request.endTime());

        WorkSegmentPatchRequest norm = normalizePatch(request);

        WorkSegmentEntity segment = persistence.lockByIdForUpdate(workSegmentId);

        // Apply scalar patch in-memory (no DB write yet)
        persistence.applyPatch(segment, norm);

        // Resolve relationships (still no DB write)
        applyRelationUpdates(segment, norm.categoryId(), norm.activityId());

        // Validate final entity state BEFORE saving/flushing
        validateRange(segment.getStartTime(), segment.getEndTime());

        // Now persist
        return saveOrThrow(segment);
    }

    /* ------------------------- DELETE ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = { "workSegments", "workSessions" }, allEntries = true)
    public void delete(Long workSegmentId) {
        persistence.deleteById(workSegmentId);
    }

    /* ------------------------- HELPERS ------------------------- */

    /* ------------------------- HELPERS: TIME ------------------------- */

    private Instant now() {
        return Instant.now();
    }

    /* ------------------------- HELPERS: NORMALIZATION ------------------------- */

    private WorkSegmentStartRequest normalizeStart(WorkSegmentStartRequest request) {
        String comment = StringSanitizer.normalizeNonNull(request.comment());
        return new WorkSegmentStartRequest(request.categoryId(), request.activityId(), comment);
    }

    private WorkSegmentPatchRequest normalizePatch(WorkSegmentPatchRequest request) {
        String comment = StringSanitizer.normalize(request.comment());
        return new WorkSegmentPatchRequest(
                request.categoryId(),
                request.activityId(),
                request.startTime(),
                request.endTime(),
                comment
        );
    }

    /* ------------------------- HELPERS: RESOLVERS (load or throw) ------------------------- */

    private WorkSegmentEntity resolveCurrentSegmentOrThrow() {
        return persistence.lockCurrentForUpdate()
                .orElseThrow(() -> new NoActiveException(Subject.WORK_SEGMENT));
    }

    private WorkSessionEntity resolveCurrentSessionOrThrow() {
        return sessionRepo.lockCurrentForUpdate()
                .orElseThrow(() -> new NoActiveException(Subject.WORK_SESSION));
    }

    private CategoryEntity resolveCategoryOrThrow(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(Subject.CATEGORY, LookupField.ID, id));
    }

    private ActivityEntity resolveActivityOrThrow(Long id) {
        return activityRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(Subject.ACTIVITY, LookupField.ID, id));
    }

    /* ------------------------- HELPERS: RULES / UPDATES ------------------------- */

    private void ensureNoOpenSegment() {
        if (persistence.lockCurrentForUpdate().isPresent()) {
            throw new AlreadyRunningException(Subject.WORK_SEGMENT);
        }
    }

    private void applyRelationUpdates(WorkSegmentEntity segment, Long categoryId, Long activityId) {
        if (categoryId != null) {
            segment.setCategory(resolveCategoryOrThrow(categoryId));
        }
        if (activityId != null) {
            segment.setActivity(resolveActivityOrThrow(activityId));
        }
    }

    private void applyStopUpdates(WorkSegmentEntity segment, WorkSegmentStopRequest request) {
        if (request.comment() != null) {
            segment.setComment(StringSanitizer.normalize(request.comment()));
        }
        applyRelationUpdates(segment, request.categoryId(), request.activityId());
    }

    /* ------------------------- HELPERS: SAVE / VALIDATE ------------------------- */

    private void validateRange(Instant start, Instant end) {
        TimeRangeUtil.requireValidOpenRange(Subject.WORK_SEGMENT, start, end);
    }

    private WorkSegmentResponse saveOrThrow(WorkSegmentEntity entity) {
        try {
            return persistence.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(Subject.WORK_SEGMENT, e);
        }
    }
}