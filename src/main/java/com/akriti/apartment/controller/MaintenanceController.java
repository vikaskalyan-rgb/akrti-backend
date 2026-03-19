package com.akriti.apartment.controller;

import com.akriti.apartment.dto.MarkPaymentRequest;
import com.akriti.apartment.dto.MessageResponse;
import com.akriti.apartment.entity.User;
import com.akriti.apartment.repository.UserRepository;
import com.akriti.apartment.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;
    @Autowired private UserRepository userRepository;

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

    @PostMapping("/reminders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendReminders(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(required = false) String flatNo) {
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year  == 0) year  = LocalDate.now().getYear();

        // Single flat reminder
        if (flatNo != null && !flatNo.isBlank()) {
            List<User> users = userRepository.findByFlatNo(flatNo);
            boolean hasEmail = users.stream()
                    .anyMatch(u -> u.getEmail() != null && !u.getEmail().isBlank());
            if (!hasEmail) {
                return ResponseEntity.ok(Map.of(
                        "message", "No email registered for flat " + flatNo,
                        "sent", 0,
                        "skipped", 1
                ));
            }
        }

        Map<String, Object> result = maintenanceService.sendReminders(month, year);
        return ResponseEntity.ok(result);
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
