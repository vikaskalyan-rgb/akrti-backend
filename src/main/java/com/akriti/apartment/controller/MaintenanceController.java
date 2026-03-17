package com.akriti.apartment.controller;

import com.akriti.apartment.dto.MarkPaymentRequest;
import com.akriti.apartment.dto.MessageResponse;
import com.akriti.apartment.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    // ── Get payments for a month (admin + resident) ───────
    @GetMapping
    public ResponseEntity<?> getMonthPayments(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();
        return ResponseEntity.ok(maintenanceService.getMonthPayments(month, year));
    }

    // ── Get summary for a month ───────────────────────────
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();
        return ResponseEntity.ok(maintenanceService.getMonthSummary(month, year));
    }

    // ── Get payments for a flat ───────────────────────────
    @GetMapping("/flat/{flatNo}")
    public ResponseEntity<?> getFlatPayments(@PathVariable String flatNo) {
        return ResponseEntity.ok(maintenanceService.getFlatPayments(flatNo));
    }

    // ── Mark payment as paid (resident + admin-residents) ─────────
    @PostMapping("/flat/{flatNo}/pay")
    public ResponseEntity<?> markPaid(
            @PathVariable String flatNo,
            @RequestParam int month,
            @RequestParam int year,
            @RequestBody MarkPaymentRequest req,
            Authentication auth) {
        try {
            String callerPhone = auth.getName();
            String callerRole  = auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("");

            // Admins can mark payment for their own flat directly
            // Residents can only mark their own flat
            if (!callerRole.equals("ROLE_ADMIN")) {
                // For residents, verify they are the payer
                return ResponseEntity.ok(
                        maintenanceService.markPaid(flatNo, month, year, req, callerPhone));
            } else {
                // Admin — allow directly, no phone check
                return ResponseEntity.ok(
                        maintenanceService.markPaidByAdmin(flatNo, month, year, req));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    // ── Send reminders to defaulters (admin only) ─────────
    @PostMapping("/reminders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendReminders(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();
        int count = maintenanceService.sendReminders(month, year);
        return ResponseEntity.ok(
            MessageResponse.builder()
                .message("Reminders sent to " + count + " defaulters")
                .success(true).build());
    }

    // ── Manually trigger due generation (admin only) ──────
    @PostMapping("/generate-dues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateDues(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();
        int count = maintenanceService.generateMonthlyDues(month, year);
        return ResponseEntity.ok(
            MessageResponse.builder()
                .message("Generated " + count + " new payment records")
                .success(true).build());
    }
}
