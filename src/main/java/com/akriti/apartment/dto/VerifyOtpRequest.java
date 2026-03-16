package com.akriti.apartment.dto;
import lombok.Data;
@Data
public class VerifyOtpRequest {
    private String phone;
    private String otp;
}
