package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "listings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Listing {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String category; // FURNITURE, ELECTRONICS, CLOTHING, VEHICLE, BOOKS, KITCHEN, FITNESS, KIDS, PROPERTY, OTHER

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false) @Builder.Default
    private Long price = 0L; // 0 = free

    @Column(name = "flat_no", length = 10)
    private String flatNo;

    @Column(name = "seller_name")
    private String sellerName;

    @Column(name = "seller_phone", length = 15)
    private String sellerPhone;

    @Column(name = "is_active") @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at") @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}