package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "step_walkers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flat_no", "walker_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepWalker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flat_no", nullable = false)
    private String flatNo;

    @Column(name = "walker_name", nullable = false)
    private String walkerName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}