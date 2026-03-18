package com.akriti.apartment.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier; // 4B, 4B_tenant, SUP
    private String password;
}