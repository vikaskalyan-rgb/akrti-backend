package com.akriti.apartment.dto;
import lombok.Data;
import java.time.LocalDate;
@Data
public class ExpenseRequest {
    private String description;
    private String category;
    private Integer amount;
    private LocalDate date;
}
