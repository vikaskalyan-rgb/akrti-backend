package com.akriti.apartment.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String identifier; // 4B or 4B_tenant
    private String email;
}