package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "ideas")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Idea {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String category; // IMPROVEMENT, REVENUE, GREEN, MAINTENANCE, OTHER

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "flat_no", length = 10)
    private String flatNo;

    @Column(name = "user_name")
    private String userName;

    @Column(nullable = false, length = 20) @Builder.Default
    private String status = "NEW"; // NEW, UNDER_REVIEW, ACCEPTED, REJECTED

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}