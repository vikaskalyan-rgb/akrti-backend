package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "community_post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "flat_no"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityPostLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @Column(name = "flat_no", nullable = false, length = 10)
    private String flatNo;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}