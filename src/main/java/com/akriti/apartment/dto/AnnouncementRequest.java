package com.akriti.apartment.dto;
import lombok.Data;
@Data
public class AnnouncementRequest {
    private String type;
    private String audience;
    private String title;
    private String body;
}
