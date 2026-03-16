package com.akriti.apartment.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String name;
    private String phone;
    private String flatNo;
}
