package com.mrfop.worktime.repository;

import com.mrfop.worktime.model.entity.WorkSegmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkSegmentRepository extends JpaRepository<WorkSegmentEntity, Long> {

    // -------------------------------------------------------------------------
    // Read queries (ordering and filtering)
    // -------------------------------------------------------------------------

    /**
     * Returns all work segments ordered by start time descending (most recent first).
     */
    List<WorkSegmentEntity> findAllByOrderByStartTimeDesc();

    /**
     * Returns the most recent open work segment (endTime is null), if any.
     */
    Optional<WorkSegmentEntity> findTopByEndTimeIsNullOrderByStartTimeDesc();

    // -------------------------------------------------------------------------
    // Concurrency control (pessimistic locks)
    // -------------------------------------------------------------------------

    /**
     * Locks the current open work segment (endTime is null) using a pessimistic write lock.
     * Used to prevent concurrent start/stop operations from creating multiple open segments.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from WorkSegmentEntity s where s.endTime is null")
    Optional<WorkSegmentEntity> lockCurrentForUpdate();

    /**
     * Locks the work segment with the given id using a pessimistic write lock.
     * Used to protect updates (patch/stop) against concurrent modifications.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from WorkSegmentEntity s where s.id = :id")
    Optional<WorkSegmentEntity> lockByIdForUpdate(@Param("id") Long id);
}