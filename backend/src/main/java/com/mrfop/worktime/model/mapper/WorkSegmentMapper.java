package com.mrfop.worktime.model.mapper;

import com.mrfop.worktime.model.entity.WorkSegmentEntity;
import com.mrfop.worktime.model.request.WorkSegmentPatchRequest;
import com.mrfop.worktime.model.request.WorkSegmentStartRequest;
import com.mrfop.worktime.model.response.WorkSegmentResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkSegmentMapper {

    public WorkSegmentResponse toResponse(WorkSegmentEntity entity) {
        if (entity == null) return null;

        // Note: relations are LAZY; this method assumes the service loaded what it needs
        // (e.g., via fetch join / entity graph) or you're still in an open session.
        var ws = entity.getWorkSession();
        var cat = entity.getCategory();
        var act = entity.getActivity();

        return new WorkSegmentResponse(
            entity.getId(),
            ws != null ? ws.getId() : null,

            cat != null ? cat.getId() : null,
            cat != null ? cat.getName() : null,

            act != null ? act.getId() : null,
            act != null ? act.getName() : null,

            entity.getStartTime(),
            entity.getEndTime(),
            entity.getComment(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public void applyPatch(WorkSegmentPatchRequest dto, WorkSegmentEntity entity) {
        if (dto == null || entity == null) return;

        // id / createdAt / updatedAt intentionally not touched
        // startDate / openFlag are generated columns (insertable=false, updatable=false) so we do not touch them
        // relationships are controlled by service (category/activity/workSession) so we do not touch them here

        // Only patch fields that are directly owned by WorkSegmentEntity and allowed here:
        if (dto.startTime() != null) {
            entity.setStartTime(dto.startTime());
        }
        if (dto.endTime() != null) {
            entity.setEndTime(dto.endTime());
        }
        if (dto.comment() != null) {
            entity.setComment(dto.comment());
        }

        // Intentionally ignored:
        // dto.categoryId(), dto.activityId()
        // Service should resolve and set entity.setCategory(...) / entity.setActivity(...)
    }

    public WorkSegmentEntity toNewEntity(WorkSegmentStartRequest dto) {
        if (dto == null) return null;

        WorkSegmentEntity entity = new WorkSegmentEntity();

        // id / createdAt / updatedAt intentionally not set
        // startDate / openFlag are generated columns (insertable=false, updatable=false) so we do not touch them

        // Relationships resolved in service:
        // entity.setWorkSession(...)
        // entity.setCategory(...)
        // entity.setActivity(...)

        // startTime / endTime are set explicitly in service for clarity (per your comment)

        // comment is also set explicitly in service (per your comment)
        // entity.setComment(dto.comment());

        return entity;
    }
}