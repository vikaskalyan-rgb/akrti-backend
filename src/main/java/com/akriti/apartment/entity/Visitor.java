package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitors")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 30)
    private String purpose;

    @Column(name = "flat_no", nullable = false, length = 10)
    private String flatNo;

    @Column(name = "resident_name")
    private String residentName;

    @Column(length = 15)
    private String phone;

    @Column(name = "vehicle_no", length = 20)
    private String vehicleNo;

    @Column(name = "in_time", nullable = false)
    @Builder.Default
    private LocalDateTime inTime = LocalDateTime.now();

    @Column(name = "out_time")
    private LocalDateTime outTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VisitorStatus status = VisitorStatus.IN;

    public enum VisitorStatus {
        IN, OUT
    }
}
