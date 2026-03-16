package com.akriti.apartment.dto;
import lombok.Data;
@Data
public class FlatRequest {
    private String flatNo;
    private Integer floor;
    private String unit;
    private String wing;
    private String ownerType;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String residentName;
    private String residentPhone;
    private String residentEmail;
    private String parkingSlot;
}
