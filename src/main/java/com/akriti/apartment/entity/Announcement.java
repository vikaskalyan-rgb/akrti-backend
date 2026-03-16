package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementType type; // NOTICE, EVENT, URGENT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Audience audience = Audience.EVERYONE;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "posted_by")
    @Builder.Default
    private String postedBy = "Admin";

    @Column(name = "posted_at")
    @Builder.Default
    private LocalDate postedAt = LocalDate.now();

    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum AnnouncementType {
        NOTICE, EVENT, URGENT
    }

    public enum Audience {
        EVERYONE, OWNERS, RESIDENTS
    }
}
