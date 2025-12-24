package com.mrfop.worktime.model.entity;

import com.mrfop.worktime.model.entity.audit.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
    name = "category",
    uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = "name"),
    indexes = {
        @Index(name = "idx_category_active", columnList = "active")
    }
)
@Getter @Setter
@NoArgsConstructor
public class CategoryEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}