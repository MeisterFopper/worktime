package com.mrfop.worktime.persistence;

import com.mrfop.worktime.exception.NotFoundException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.entity.CategoryEntity;
import com.mrfop.worktime.model.enums.ActiveStatus;
import com.mrfop.worktime.model.mapper.CategoryMapper;
import com.mrfop.worktime.model.request.CategoryCreateRequest;
import com.mrfop.worktime.model.request.CategoryPatchRequest;
import com.mrfop.worktime.model.response.CategoryResponse;
import com.mrfop.worktime.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryPersistence {

    private final CategoryRepository repo;
    private final CategoryMapper mapper;

    /* ------------------------- READ (mapped DTOs) ------------------------- */

    public List<CategoryResponse> findByStatus(ActiveStatus status) {
        ActiveStatus effective = (status == null) ? ActiveStatus.ALL : status;

        List<CategoryEntity> entities = switch (effective) {
            case ACTIVE -> repo.findAllByActiveTrueOrderByNameAsc();
            case INACTIVE -> repo.findAllByActiveFalseOrderByNameAsc();
            case ALL -> repo.findAllByOrderByNameAsc();
        };

        return entities.stream().map(mapper::toResponse).toList();
    }

    /* ------------------------- DB primitives ------------------------- */

    public CategoryEntity getByIdOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException(Subject.CATEGORY, LookupField.ID, id));
    }

    public boolean existsByNameIgnoreCase(String name) {
        return repo.existsByNameIgnoreCase(name);
    }

    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return repo.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    /* ------------------------- Write helpers (map + save + flush) ------------------------- */

    public CategoryResponse createAndFlush(CategoryCreateRequest request) {
        CategoryEntity entity = mapper.toNewEntity(request);
        CategoryEntity saved = repo.save(entity);
        repo.flush();
        return mapper.toResponse(saved);
    }

    public CategoryResponse patchAndFlush(CategoryEntity entity, CategoryPatchRequest request) {
        mapper.patchEntity(request, entity);
        CategoryEntity saved = repo.save(entity);
        repo.flush();
        return mapper.toResponse(saved);
    }
}