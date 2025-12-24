package com.mrfop.worktime.repository;

import com.mrfop.worktime.model.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // -------------------------------------------------------------------------
    // Uniqueness checks (case-insensitive) for Category.name
    // -------------------------------------------------------------------------

    /**
     * Checks whether a category with the given name already exists (case-insensitive).
     * Commonly used during create validation.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Checks whether a category with the given name already exists (case-insensitive),
     * excluding the category with the provided id.
     * Commonly used during update validation.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // -------------------------------------------------------------------------
    // Queries by active flag, ordered alphabetically by name
    // -------------------------------------------------------------------------

    /**
     * Returns all categories ordered by name ascending.
     */
    List<CategoryEntity> findAllByOrderByNameAsc();

    /**
     * Returns only active categories ordered by name ascending.
     */
    List<CategoryEntity> findAllByActiveTrueOrderByNameAsc();

    /**
     * Returns only inactive categories ordered by name ascending.
     */
    List<CategoryEntity> findAllByActiveFalseOrderByNameAsc();
}