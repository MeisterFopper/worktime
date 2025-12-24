package com.mrfop.worktime.service;

import com.mrfop.worktime.exception.ConflictException;
import com.mrfop.worktime.exception.NameAlreadyExistsException;
import com.mrfop.worktime.exception.ValidationException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.entity.CategoryEntity;
import com.mrfop.worktime.model.enums.ActiveStatus;
import com.mrfop.worktime.model.request.CategoryCreateRequest;
import com.mrfop.worktime.model.request.CategoryPatchRequest;
import com.mrfop.worktime.model.response.CategoryResponse;
import com.mrfop.worktime.persistence.CategoryPersistence;
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
public class CategoryService {

    private final CategoryPersistence persistence;

    /* ------------------------- READ ------------------------- */

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categories", key = "'status:' + (#status == null ? 'ALL' : #status.name())")
    public List<CategoryResponse> findByStatus(ActiveStatus status) {
        return persistence.findByStatus(status);
    }

    /* ------------------------- CREATE ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryResponse create(CategoryCreateRequest request) {
        CategoryCreateRequest norm = normalizeAndValidateCreate(request);
        try {
            return persistence.createAndFlush(norm);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(Subject.CATEGORY, e);
        }
    }

    /* ------------------------- PATCH ------------------------- */

    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryResponse patch(Long categoryId, CategoryPatchRequest request) {
        CategoryPatchRequest norm = normalizeAndValidatePatch(categoryId, request);
        CategoryEntity entity = persistence.getByIdOrThrow(categoryId);

        try {
            return persistence.patchAndFlush(entity, norm);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(Subject.CATEGORY, e);
        }
    }

    /* ------------------------- HELPERS ------------------------- */

    private CategoryCreateRequest normalizeAndValidateCreate(CategoryCreateRequest request) {
        String rawName = request.name();
        String name = StringSanitizer.normalizeNonNull(rawName);

        if (name.isBlank()) {
            throw new ValidationException(Subject.CATEGORY, LookupField.NAME, rawName);
        }

        if (persistence.existsByNameIgnoreCase(name)) {
            throw new NameAlreadyExistsException(Subject.CATEGORY, name);
        }

        String description = StringSanitizer.normalizeNonNull(request.description());
        return new CategoryCreateRequest(name, description, request.active());
    }

    private CategoryPatchRequest normalizeAndValidatePatch(Long categoryId, CategoryPatchRequest request) {
        String rawName = request.name();
        String name = null;

        if (rawName != null) {
            name = StringSanitizer.normalizeNonNull(rawName);

            if (name.isBlank()) {
                throw new ValidationException(Subject.CATEGORY, LookupField.NAME, rawName);
            }

            if (persistence.existsByNameIgnoreCaseAndIdNot(name, categoryId)) {
                throw new NameAlreadyExistsException(Subject.CATEGORY, name);
            }
        }

        String description = StringSanitizer.normalize(request.description());
        return new CategoryPatchRequest(name, description, request.active());
    }
}