package com.mrfop.worktime.persistence;

import com.mrfop.worktime.exception.NotFoundException;
import com.mrfop.worktime.exception.base.LookupField;
import com.mrfop.worktime.exception.base.Subject;
import com.mrfop.worktime.model.entity.ActivityEntity;
import com.mrfop.worktime.model.enums.ActiveStatus;
import com.mrfop.worktime.model.mapper.ActivityMapper;
import com.mrfop.worktime.model.request.ActivityCreateRequest;
import com.mrfop.worktime.model.request.ActivityPatchRequest;
import com.mrfop.worktime.model.response.ActivityResponse;
import com.mrfop.worktime.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ActivityPersistence {

    private final ActivityRepository repo;
    private final ActivityMapper mapper;

    /* ------------------------- READ (mapped DTOs) ------------------------- */

    public List<ActivityResponse> findByStatus(ActiveStatus status) {
        ActiveStatus effective = (status == null) ? ActiveStatus.ALL : status;

        List<ActivityEntity> entities = switch (effective) {
            case ACTIVE -> repo.findAllByActiveTrueOrderByNameAsc();
            case INACTIVE -> repo.findAllByActiveFalseOrderByNameAsc();
            case ALL -> repo.findAllByOrderByNameAsc();
        };

        return entities.stream().map(mapper::toResponse).toList();
    }

    /* ------------------------- DB primitives ------------------------- */

    public ActivityEntity getByIdOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException(Subject.ACTIVITY, LookupField.ID, id));
        
    }

    public boolean existsByNameIgnoreCase(String name) {
        return repo.existsByNameIgnoreCase(name);
    }

    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return repo.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    /* ------------------------- Write helpers (map + save + flush) ------------------------- */

    public ActivityResponse createAndFlush(ActivityCreateRequest request) {
        ActivityEntity entity = mapper.toNewEntity(request);
        ActivityEntity saved = repo.save(entity);
        repo.flush();
        return mapper.toResponse(saved);
    }

    public ActivityResponse patchAndFlush(ActivityEntity entity, ActivityPatchRequest request) {
        mapper.patchEntity(request, entity);
        ActivityEntity saved = repo.save(entity);
        repo.flush();
        return mapper.toResponse(saved);
    }
}