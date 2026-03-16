package com.akriti.apartment.controller;

import com.akriti.apartment.dto.*;
import com.akriti.apartment.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<?> getByMonth(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();
        return ResponseEntity.ok(expenseService.getByMonth(month, year));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();
        return ResponseEntity.ok(expenseService.getMonthlySummary(month, year));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody ExpenseRequest req) {
        try {
            return ResponseEntity.ok(expenseService.create(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.ok(
            MessageResponse.builder().message("Deleted").success(true).build());
    }
}
