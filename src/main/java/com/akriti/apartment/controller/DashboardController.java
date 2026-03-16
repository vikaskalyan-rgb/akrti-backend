package com.akriti.apartment.controller;

import com.akriti.apartment.service.*;
import com.akriti.apartment.repository.*;
import com.akriti.apartment.entity.Complaint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private MaintenanceService maintenanceService;
    @Autowired private ExpenseService expenseService;
    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private VisitorService visitorService;
    @Autowired private FlatRepository flatRepository;

    @Value("${app.monthly.maintenance:4200}")
    private int monthlyAmount;

    @GetMapping
    public ResponseEntity<?> getDashboard(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {

        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();

        Map<String, Object> dashboard = new LinkedHashMap<>();

        // Maintenance summary
        dashboard.put("maintenance", maintenanceService.getMonthSummary(month, year));

        // Expense summary
        dashboard.put("expenses", expenseService.getMonthlySummary(month, year));

        // Complaint counts
        dashboard.put("complaints", Map.of(
            "open",       complaintRepository.countByStatus(Complaint.Status.OPEN),
            "inProgress", complaintRepository.countByStatus(Complaint.Status.IN_PROGRESS),
            "resolved",   complaintRepository.countByStatus(Complaint.Status.RESOLVED)
        ));

        // Visitor stats
        dashboard.put("visitors", visitorService.getStats());

        // Flat stats
        var flats = flatRepository.findByIsActiveTrue();
        long occupied = flats.stream().filter(f -> !f.isVacant() && f.getFloor() > 0).count();
        long vacant   = flats.stream().filter(f -> f.isVacant()  && f.getFloor() > 0).count();
        long rented   = flats.stream().filter(f -> f.getOwnerType() != null
            && f.getOwnerType().name().equals("RENTED")).count();
        dashboard.put("flats", Map.of(
            "total", flats.stream().filter(f -> f.getFloor() > 0).count(),
            "occupied", occupied,
            "vacant", vacant,
            "rented", rented
        ));

        // Society fund (static for now — wire to a Fund table later)
        dashboard.put("societyFund", Map.of(
            "currentBalance", 382000,
            "lastUpdated", LocalDate.now().toString()
        ));

        return ResponseEntity.ok(dashboard);
    }

    // 6-month trend for reports
    @GetMapping("/trend")
    public ResponseEntity<?> getTrend(@RequestParam(defaultValue = "6") int months) {
        var trend = new java.util.ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            int m = d.getMonthValue();
            int y = d.getYear();
            Map<String, Object> summary = maintenanceService.getMonthSummary(m, y);
            var expenseList = expenseService.getByMonth(m, y);
            int totalExpenses = expenseList.stream().mapToInt(e -> e.getAmount()).sum();
            summary.put("expenses", totalExpenses);
            summary.put("surplus", (int) summary.get("collected") - totalExpenses);
            trend.add(summary);
        }
        return ResponseEntity.ok(trend);
    }
}
