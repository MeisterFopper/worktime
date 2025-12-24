package com.mrfop.worktime.service;

import com.mrfop.worktime.exception.ConflictException;
import com.mrfop.worktime.exception.NameAlreadyExistsException;
import com.mrfop.worktime.exception.ValidationException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.entity.ActivityEntity;
import com.mrfop.worktime.model.enums.ActiveStatus;
import com.mrfop.worktime.model.request.ActivityCreateRequest;
import com.mrfop.worktime.model.request.ActivityPatchRequest;
import com.mrfop.worktime.model.response.ActivityResponse;
import com.mrfop.worktime.persistence.ActivityPersistence;
import com.mrfop.worktime.util.StringSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityPersistence persistence;

    /* ------------------------- READ ------------------------- */

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "activities", key = "'status:' + (#status == null ? 'ALL' : #status.name())")
    public List<ActivityResponse> findByStatus(ActiveStatus status) {
        return persistence.findByStatus(status);
    }

    /* ------------------------- CREATE ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = "activities", allEntries = true)
    public ActivityResponse create(ActivityCreateRequest request) {
        ActivityCreateRequest norm = normalizeAndValidateCreate(request);
        try {
            return persistence.createAndFlush(norm);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(Subject.ACTIVITY, e);
        }
    }

    /* ------------------------- PATCH ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = "activities", allEntries = true)
    public ActivityResponse patch(Long activityId, ActivityPatchRequest request) {
        ActivityPatchRequest norm = normalizeAndValidatePatch(activityId, request);
        ActivityEntity entity = persistence.getByIdOrThrow(activityId);

        try {
            return persistence.patchAndFlush(entity, norm);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(Subject.ACTIVITY, e);
        }
    }

    /* ------------------------- HELPERS ------------------------- */

    private ActivityCreateRequest normalizeAndValidateCreate(ActivityCreateRequest request) {
        String name = StringSanitizer.normalizeNonNull(request.name());

        if (name.isBlank()) {
            throw new ValidationException(Subject.ACTIVITY, LookupField.NAME, null);
        }

        if (persistence.existsByNameIgnoreCase(name)) {
            throw new NameAlreadyExistsException(Subject.ACTIVITY, name);
        }

        String description = StringSanitizer.normalizeNonNull(request.description());
        return new ActivityCreateRequest(name, description, request.active());
    }

    private ActivityPatchRequest normalizeAndValidatePatch(Long activityId, ActivityPatchRequest request) {
        String name = request.name();

        if (name != null) {
            name = StringSanitizer.normalizeNonNull(name);

            if (name.isBlank()) {
                throw new ValidationException(Subject.ACTIVITY, LookupField.NAME, null);
            }

            if (persistence.existsByNameIgnoreCaseAndIdNot(name, activityId)) {
                throw new NameAlreadyExistsException(Subject.ACTIVITY, name);
            }
        }

        String description = StringSanitizer.normalize(request.description());
        return new ActivityPatchRequest(name, description, request.active());
    }
}