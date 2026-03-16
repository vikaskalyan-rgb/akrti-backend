package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flats")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Flat {

    @Id
    @Column(name = "flat_no", length = 10)
    private String flatNo;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false, length = 5)
    private String unit;

    @Column(nullable = false, length = 10)
    private String wing; // North / South / Ground

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerType ownerType; // OWNER_OCCUPIED, RENTED, VACANT

    // Owner info — always present
    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_phone", length = 15)
    private String ownerPhone;

    @Column(name = "owner_email")
    private String ownerEmail;

    // Resident info — null if vacant
    @Column(name = "resident_name")
    private String residentName;

    @Column(name = "resident_phone", length = 15)
    private String residentPhone;

    @Column(name = "resident_email")
    private String residentEmail;

    @Column(name = "parking_slot", length = 10)
    private String parkingSlot;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum OwnerType {
        OWNER_OCCUPIED, RENTED, VACANT
    }

    public boolean isVacant() {
        return ownerType == OwnerType.VACANT;
    }

    // Who pays maintenance: tenant if rented, owner otherwise
    public String getPayerName() {
        if (ownerType == OwnerType.RENTED) return residentName;
        return ownerName;
    }

    public String getPayerPhone() {
        if (ownerType == OwnerType.RENTED) return residentPhone;
        return ownerPhone;
    }
}
