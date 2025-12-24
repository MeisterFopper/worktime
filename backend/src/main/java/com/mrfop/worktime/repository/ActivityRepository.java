package com.mrfop.worktime.repository;

import com.mrfop.worktime.model.entity.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {

    // -------------------------------------------------------------------------
    // Uniqueness checks (case-insensitive) for Activity.name
    // -------------------------------------------------------------------------

    /**
     * Checks whether an activity with the given name already exists (case-insensitive).
     * Commonly used during create validation.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Checks whether an activity with the given name already exists (case-insensitive),
     * excluding the activity with the provided id.
     * Commonly used during update validation.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // -------------------------------------------------------------------------
    // Queries by active flag, ordered alphabetically by name
    // -------------------------------------------------------------------------

    /**
     * Returns all activities ordered by name ascending.
     */
    List<ActivityEntity> findAllByOrderByNameAsc();

    /**
     * Returns only active activities ordered by name ascending.
     */
    List<ActivityEntity> findAllByActiveTrueOrderByNameAsc();

    /**
     * Returns only inactive activities ordered by name ascending.
     */
    List<ActivityEntity> findAllByActiveFalseOrderByNameAsc();
}