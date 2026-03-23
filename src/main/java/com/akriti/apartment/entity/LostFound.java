package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "lost_found")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LostFound {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String type; // LOST or FOUND

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String location;

    @Column(name = "flat_no", length = 10)
    private String flatNo;

    @Column(name = "posted_by")
    private String postedBy;

    @Column(nullable = false)
    @Builder.Default
    private String status = "OPEN"; // OPEN, RETURNED

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at") @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}