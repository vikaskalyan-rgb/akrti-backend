package com.akriti.apartment.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String flatNo;
    private String password;
}