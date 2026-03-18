package com.akriti.apartment.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String flatNo;
    private String email;
}