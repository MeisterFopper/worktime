package com.mrfop.worktime.repository;

import com.mrfop.worktime.model.entity.WorkSessionEntity;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSessionEntity, Long> {

    // -------------------------------------------------------------------------
    // Read queries (ordering and filtering)
    // -------------------------------------------------------------------------

    /**
     * Returns the most recent open work session (endTime is null), if any.
     */
    Optional<WorkSessionEntity> findTopByEndTimeIsNullOrderByStartTimeDesc();

    /**
     * Returns all work sessions ordered by start time descending (most recent first).
     */
    List<WorkSessionEntity> findAllByOrderByStartTimeDesc();

    /**
     * TODO: Description of the query that returns all work sessions overlapping
     */
    @EntityGraph(attributePaths = {
        "segments",
        "segments.category",
        "segments.activity"
    })
    @Query("""
        select ws
        from WorkSessionEntity ws
        where (:toExclusive is null or ws.startTime < :toExclusive)
          and (:fromInclusive is null or coalesce(ws.endTime, :nowUtc) > :fromInclusive)
        order by ws.startTime desc
    """)
    List<WorkSessionEntity> findOverlappingWithSegments(
            @Param("nowUtc") Instant nowUtc,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );

    // -------------------------------------------------------------------------
    // Concurrency control (pessimistic locks)
    // -------------------------------------------------------------------------

    /**
     * Locks the current open work session (endTime is null) using a pessimistic write lock.
     * Used to prevent concurrent start/stop operations from creating multiple open sessions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WorkSessionEntity w where w.endTime is null")
    Optional<WorkSessionEntity> lockCurrentForUpdate();

    /**
     * Locks the work session with the given id using a pessimistic write lock.
     * Used to protect updates (patch/stop) against concurrent modifications.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WorkSessionEntity w where w.id = :id")
    Optional<WorkSessionEntity> lockByIdForUpdate(Long id);
}