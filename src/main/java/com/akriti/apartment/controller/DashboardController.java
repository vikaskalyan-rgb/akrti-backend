package com.akriti.apartment.controller;

import com.akriti.apartment.service.*;
import com.akriti.apartment.repository.*;
import com.akriti.apartment.entity.Complaint;
import com.akriti.apartment.entity.MaintenancePayment;
import com.akriti.apartment.entity.Delivery;
import com.akriti.apartment.entity.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private MaintenanceService maintenanceService;
    @Autowired private ExpenseService expenseService;
    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private VisitorService visitorService;
    @Autowired private FlatRepository flatRepository;
    @Autowired private SocietySettingsRepository settingsRepo;

    // ── repositories for activity feed ──
    @Autowired private MaintenancePaymentRepository maintenancePaymentRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private AnnouncementRepository announcementRepository;

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

        // Society fund
        int openingBalance = settingsRepo.findById("opening_balance")
                .map(s -> Integer.parseInt(s.getValue())).orElse(0);

        int allTimeCollected = maintenanceService.getAllTimeCollected();
        int allTimeExpenses  = expenseService.getAllTimeExpenses();
        int corpus = openingBalance + allTimeCollected - allTimeExpenses;

        dashboard.put("societyFund", Map.of(
                "currentBalance", corpus,
                "lastUpdated",    LocalDate.now().toString()
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

    // ── Activity feed: recent payments, complaints, announcements, deliveries ──
    @GetMapping("/activity-feed")
    public ResponseEntity<?> getActivityFeed() {
        List<Map<String, Object>> feed = new ArrayList<>();

        // Recent PAID payments (last 5)
        maintenancePaymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.PAID)
                .sorted((a, b) -> {
                    if (a.getUpdatedAt() == null || b.getUpdatedAt() == null) return 0;
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .limit(5)
                .forEach(p -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type",      "payment");
                    item.put("text",      "Flat " + p.getFlatNo() + " paid \u20b9" + p.getPaidAmount());
                    item.put("sub",       "Maintenance");
                    item.put("timestamp", p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : "");
                    feed.add(item);
                });

        // Recent OPEN complaints (last 3)
        complaintRepository.findAll().stream()
                .filter(c -> c.getStatus() == Complaint.Status.OPEN)
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(3)
                .forEach(c -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type",      "complaint");
                    item.put("text",      c.getTitle());
                    item.put("sub",       "Complaint \u00b7 Flat " + c.getFlatNo());
                    item.put("timestamp", c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
                    feed.add(item);
                });

        // Recent announcements (last 2)
        announcementRepository.findAllByOrderByIsPinnedDescPostedAtDesc().stream()
                .limit(2)
                .forEach(a -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type",      "announcement");
                    item.put("text",      a.getTitle());
                    item.put("sub",       "Announcement");
                    item.put("timestamp", a.getPostedAt() != null ? a.getPostedAt().toString() : "");
                    feed.add(item);
                });

        // Recent PENDING deliveries (last 3)
        deliveryRepository.findByStatusOrderByLoggedAtDesc("PENDING").stream()
                .limit(3)
                .forEach(d -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type",      "delivery");
                    item.put("text",      "Parcel for Flat " + d.getFlatNo());
                    item.put("sub",       "Delivery");
                    item.put("timestamp", d.getLoggedAt() != null ? d.getLoggedAt().toString() : "");
                    feed.add(item);
                });

        // Sort all by timestamp desc, return top 8
        feed.sort((a, b) -> {
            String ta = (String) a.getOrDefault("timestamp", "");
            String tb = (String) b.getOrDefault("timestamp", "");
            return tb.compareTo(ta);
        });

        return ResponseEntity.ok(feed.stream().limit(8).collect(Collectors.toList()));
    }
}