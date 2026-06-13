package com.akriti.apartment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String label;

    @Column(name = "pos_x", nullable = false)
    private Double posX = 0.0;

    @Column(name = "pos_z", nullable = false)
    private Double posZ = 0.0;

    @Column(nullable = false)
    private Double rotation = 0.0;

    @Column(name = "car_capacity", nullable = false)
    private Integer carCapacity = 1;

    @Column(name = "bike_capacity", nullable = false)
    private Integer bikeCapacity = 0;

    @Column(name = "assigned_flat", length = 10)
    private String assignedFlat;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    // ── Getters / Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }

    public Double getPosZ() { return posZ; }
    public void setPosZ(Double posZ) { this.posZ = posZ; }

    public Double getRotation() { return rotation; }
    public void setRotation(Double rotation) { this.rotation = rotation; }

    public Integer getCarCapacity() { return carCapacity; }
    public void setCarCapacity(Integer carCapacity) { this.carCapacity = carCapacity; }

    public Integer getBikeCapacity() { return bikeCapacity; }
    public void setBikeCapacity(Integer bikeCapacity) { this.bikeCapacity = bikeCapacity; }

    public String getAssignedFlat() { return assignedFlat; }
    public void setAssignedFlat(String assignedFlat) { this.assignedFlat = assignedFlat; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}