package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "activity_rsvps",
        uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "flat_no"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityRsvp {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private WeeklyActivity activity;

    @Column(name = "flat_no", nullable = false, length = 10)
    private String flatNo;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}