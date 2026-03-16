package com.akriti.apartment.dto;
import lombok.Data;
@Data
public class MarkPaymentRequest {
    private String paymentMode;
    private String transactionRef;
}
