package com.mrfop.worktime.model.entity;

import com.mrfop.worktime.model.entity.audit.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "work_segment",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_work_segment_one_open_per_session",
        columnNames = {"work_session_id", "open_flag"}
    ),
    indexes = {
        @Index(name = "idx_work_segment_start_date", columnList = "start_date"),

        // Reporting-friendly indexes from the new schema
        @Index(name = "idx_work_segment_category_start", columnList = "category_id,start_time"),
        @Index(name = "idx_work_segment_activity_start", columnList = "activity_id,start_time"),
        @Index(name = "idx_work_segment_category_activity_start", columnList = "category_id,activity_id,start_time"),

        @Index(name = "idx_work_segment_session_start", columnList = "work_session_id,start_time"),
        @Index(name = "idx_work_segment_session_end", columnList = "work_session_id,end_time")
    }
)
@Getter @Setter
@NoArgsConstructor
public class WorkSegmentEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_session_id", nullable = false)
    private WorkSessionEntity workSession;

    // Category tag
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    // Activity tag
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private ActivityEntity activity;

    @NotNull
    @Column(name = "start_time", nullable = false, columnDefinition = "datetime(3)")
    private Instant startTime; // UTC

    @Column(name = "end_time", columnDefinition = "datetime(3)")
    private Instant endTime;   // UTC

    @Size(max = 500)
    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "start_date", insertable = false, updatable = false)
    private LocalDate startDate;

    @Column(name = "open_flag", insertable = false, updatable = false)
    private Boolean openFlag;
}