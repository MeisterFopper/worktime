package com.mrfop.worktime.model.entity;

import com.mrfop.worktime.model.entity.audit.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "work_session",
    uniqueConstraints = @UniqueConstraint(name = "uk_work_session_one_open", columnNames = "open_flag"),
    indexes = {
        @Index(name = "idx_work_session_start",      columnList = "start_time"),
        @Index(name = "idx_work_session_end",        columnList = "end_time"),
        @Index(name = "idx_work_session_start_date", columnList = "start_date")
    }
)
@Getter @Setter
@NoArgsConstructor
public class WorkSessionEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "start_time", nullable = false, columnDefinition = "datetime(3)")
    private Instant startTime;  // UTC

    @Column(name = "end_time", columnDefinition = "datetime(3)")
    private Instant endTime;    // UTC

    @Column(name = "start_date", insertable = false, updatable = false)
    private LocalDate startDate;

    @Column(name = "open_flag", insertable = false, updatable = false)
    private Boolean openFlag;

    // Segments relation
    @OneToMany(mappedBy = "workSession", fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("startTime ASC")
    private List<WorkSegmentEntity> segments = new ArrayList<>();

    // Convenience methods
    public void addSegment(WorkSegmentEntity s) {
        segments.add(s);
        s.setWorkSession(this);
    }

    public void removeSegment(WorkSegmentEntity s) {
        segments.remove(s);
        s.setWorkSession(null);
    }
}