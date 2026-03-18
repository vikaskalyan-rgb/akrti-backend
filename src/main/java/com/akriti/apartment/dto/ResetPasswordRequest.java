package com.akriti.apartment.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String flatNo;
    private String email;
    private String otp;
    private String newPassword;
}