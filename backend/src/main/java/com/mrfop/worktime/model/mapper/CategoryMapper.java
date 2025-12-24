package com.mrfop.worktime.model.mapper;

import com.mrfop.worktime.model.entity.CategoryEntity;
import com.mrfop.worktime.model.request.CategoryCreateRequest;
import com.mrfop.worktime.model.request.CategoryPatchRequest;
import com.mrfop.worktime.model.response.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(CategoryEntity e) {
        if (e == null) return null;

        return new CategoryResponse(
            e.getId(),
            e.getName(),
            e.getDescription(),
            e.isActive(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

    public CategoryEntity toNewEntity(CategoryCreateRequest r) {
        if (r == null) return null;

        CategoryEntity e = new CategoryEntity();
        // id / createdAt / updatedAt intentionally not set
        e.setName(r.name());
        e.setDescription(r.description());
        e.setActive(r.active());
        return e;
    }

    public void patchEntity(CategoryPatchRequest r, CategoryEntity e) {
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