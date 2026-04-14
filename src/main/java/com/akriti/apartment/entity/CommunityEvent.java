package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity @Table(name = "community_events")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String category; // TUITION, COMPETITION, MOVIE, MUSIC, ART, FITNESS, LANGUAGE, CLASS, OTHER

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(length = 100)
    private String location;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "flat_no", length = 10)
    private String flatNo;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}