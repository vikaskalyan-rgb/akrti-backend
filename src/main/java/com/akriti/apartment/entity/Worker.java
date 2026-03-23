package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkerRole role;

    @Column(length = 15)
    private String phone;

    @Column
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    // Only for SECURITY role
    @Enumerated(EnumType.STRING)
    @Column
    private Shift shift;

    @Column(name = "id_proof_type", length = 30)
    private String idProofType;  // Aadhaar, Voter ID, Driving License

    @Column(name = "id_proof_number", length = 50)
    private String idProofNumber;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "monthly_salary")
    private Integer monthlySalary;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "added_by_flat", length = 10)
    private String addedByFlat;

    public enum WorkerRole {
        SECURITY, SUPERVISOR, MAID, ELECTRICIAN, PLUMBER, OTHER
    }

    public enum Shift {
        DAY, NIGHT, BOTH
    }
}