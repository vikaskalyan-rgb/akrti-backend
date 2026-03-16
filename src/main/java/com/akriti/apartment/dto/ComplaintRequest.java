package com.akriti.apartment.dto;
import lombok.Data;
@Data
public class ComplaintRequest {
    private String category;
    private String title;
    private String description;
    private String priority;
}
