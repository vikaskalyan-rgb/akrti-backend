package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "watchman_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"log_date", "hour_slot"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchmanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "hour_slot", nullable = false)
    private Integer hourSlot; // 22,23,0,1,2,3,4,5

    @Column(name = "logged_at", nullable = false)
    @Builder.Default
    private LocalDateTime loggedAt = LocalDateTime.now();

    @Column(name = "logged_by", length = 20)
    @Builder.Default
    private String loggedBy = "SUP";
}