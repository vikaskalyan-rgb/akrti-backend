package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flat_no", nullable = false, length = 10)
    private String flatNo;

    @Column(name = "resident_name", nullable = false)
    private String residentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.OPEN;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDate updatedAt = LocalDate.now();

    public enum Category {
        PLUMBING, ELECTRICAL, SECURITY, SANITATION, NOISE, STRUCTURAL, OTHER
    }

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED
    }
}
