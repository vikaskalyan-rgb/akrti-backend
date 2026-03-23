package com.akriti.apartment.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "nearby_places")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NearbyPlace {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false, length = 30) private String category;
    @Column(columnDefinition = "TEXT") private String address;
    @Column(length = 15) private String phone;
    @Column(name = "maps_url", columnDefinition = "TEXT") private String mapsUrl;
    @Column(length = 20) private String distance;
    @Column(name = "added_by", length = 20) private String addedBy;
    @Column(name = "is_active", nullable = false) @Builder.Default private Boolean isActive = true;
    @Column(name = "created_at") @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}