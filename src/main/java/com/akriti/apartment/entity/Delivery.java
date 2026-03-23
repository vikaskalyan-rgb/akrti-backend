package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "deliveries")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Delivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flat_no", nullable = false, length = 10)
    private String flatNo;

    @Column(name = "courier_name", length = 50)
    private String courierName;

    @Column(length = 100)
    private String description;

    @Column(name = "logged_by", length = 20)
    private String loggedBy;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, COLLECTED

    @Column(name = "logged_at") @Builder.Default
    private LocalDateTime loggedAt = LocalDateTime.now();

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;
}