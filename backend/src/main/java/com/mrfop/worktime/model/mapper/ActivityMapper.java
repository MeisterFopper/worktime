package com.mrfop.worktime.model.mapper;

import com.mrfop.worktime.model.entity.ActivityEntity;
import com.mrfop.worktime.model.request.ActivityCreateRequest;
import com.mrfop.worktime.model.request.ActivityPatchRequest;
import com.mrfop.worktime.model.response.ActivityResponse;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public ActivityResponse toResponse(ActivityEntity e) {
        if (e == null) return null;

        return new ActivityResponse(
            e.getId(),
            e.getName(),
            e.getDescription(),
            e.isActive(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

    public ActivityEntity toNewEntity(ActivityCreateRequest r) {
        if (r == null) return null;

        ActivityEntity e = new ActivityEntity();
        // id / createdAt / updatedAt intentionally not set
        e.setName(r.name());
        e.setDescription(r.description());
        e.setActive(r.active());
        return e;
    }

    public void patchEntity(ActivityPatchRequest r, ActivityEntity e) {
        if (r == null || e == null) return;

        // id / createdAt / updatedAt intentionally not touched

        if (r.name() != null) {
            e.setName(r.name());
        }
        if (r.description() != null) {
            e.setDescription(r.description());
        }
        if (r.active() != null) {
            e.setActive(r.active());
        }
    }
}