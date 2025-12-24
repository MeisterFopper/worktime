package com.mrfop.worktime.model.mapper;

import com.mrfop.worktime.model.entity.WorkSessionEntity;
import com.mrfop.worktime.model.request.WorkSessionPatchRequest;
import com.mrfop.worktime.model.response.WorkSessionResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkSessionMapper {

    public WorkSessionResponse toResponse(WorkSessionEntity entity) {
        if (entity == null) return null;

        return new WorkSessionResponse(
            entity.getId(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public void applyPatch(WorkSessionPatchRequest request, WorkSessionEntity entity) {
        if (request == null || entity == null) return;

        // id / createdAt / updatedAt intentionally not touched
        // startDate / openFlag are generated columns (insertable=false, updatable=false) so we do not touch them

        if (request.startTime() != null) {
            entity.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            entity.setEndTime(request.endTime());
        }
    }
}