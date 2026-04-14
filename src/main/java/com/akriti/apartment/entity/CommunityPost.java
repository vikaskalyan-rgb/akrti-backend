package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "community_posts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityPost {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String category; // POEM, KAVITHAI, STORY, QUOTE, OTHER

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "flat_no", length = 10)
    private String flatNo;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}