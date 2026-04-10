package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "place_reviews")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PlaceReview {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private NearbyPlace place;

    @Column(name = "flat_no", nullable = false, length = 10)
    private String flatNo;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "review_text", columnDefinition = "TEXT", nullable = false)
    private String reviewText;

    @Column(nullable = false)
    @Builder.Default
    private Integer rating = 5; // 1–5 stars

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}