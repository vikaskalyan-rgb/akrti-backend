package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "society_settings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SocietySetting {
    @Id
    @Column(length = 100)
    private String key;

    @Column(nullable = false, length = 500)
    private String value;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void touch() { updatedAt = LocalDateTime.now(); }
}