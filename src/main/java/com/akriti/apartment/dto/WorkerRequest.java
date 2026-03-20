package com.akriti.apartment.dto;

import lombok.Data;

@Data
public class WorkerRequest {
    private String name;
    private String role;
    private String phone;
    private String email;
    private String address;
    private String shift;
    private String idProofType;
    private String idProofNumber;
    private String joiningDate;
    private Integer monthlySalary;
    private String notes;
    private Boolean isActive;
}