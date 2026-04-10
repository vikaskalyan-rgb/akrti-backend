package com.akriti.apartment.service;

import com.akriti.apartment.dto.MarkPaymentRequest;
import com.akriti.apartment.entity.*;
import com.akriti.apartment.repository.*;
import com.akriti.apartment.websocket.WebSocketPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);

    @Autowired private MaintenancePaymentRepository paymentRepository;
    @Autowired private FlatRepository               flatRepository;
    @Autowired private UserRepository               userRepository;
    @Autowired private EmailService                 emailService;
    @Autowired private WebSocketPublisher           wsPublisher;

    @Value("${app.monthly.maintenance:4200}")
    private int monthlyAmount;

    public int getAllTimeCollected() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.PAID
                        || p.getStatus() == MaintenancePayment.PaymentStatus.PARTIAL)
                .mapToInt(p -> p.getPaidAmount() != null ? p.getPaidAmount() : 0)
                .sum();
    }

    // ── Get all payments for a month ──────────────────────
    public List<MaintenancePayment> getMonthPayments(int month, int year) {
        return paymentRepository.findByMonthAndYear(month, year);
    }

    // ── Get payments for a flat ───────────────────────────
    public List<MaintenancePayment> getFlatPayments(String flatNo) {
        return paymentRepository.findByFlatNoOrderByYearDescMonthDesc(flatNo);
    }

    // ── Get single payment ────────────────────────────────
    public MaintenancePayment getPayment(String flatNo, int month, int year) {
        return paymentRepository.findByFlatNoAndMonthAndYear(flatNo, month, year)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
    }

    // ── Shared payment logic (used by admin + resident) ───
    private MaintenancePayment applyPayment(MaintenancePayment payment,
                                            MarkPaymentRequest req) {
        int due        = payment.getAmount() != null ? payment.getAmount() : monthlyAmount;
        int paidAmount = req.getPaidAmount() != null ? req.getPaidAmount() : due;

        MaintenancePayment.PaymentMode mode = MaintenancePayment.PaymentMode.valueOf(
                req.getPaymentMode().toUpperCase().replace(" ", "_")
        );

        payment.setPaidAmount(paidAmount);
        payment.setPaidOn(LocalDate.now());
        payment.setPaymentMode(mode);
        payment.setTransactionRef(req.getTransactionRef());
        payment.setMarkedByResident(true);
        payment.setUpdatedAt(LocalDateTime.now());

        // Determine status based on amount paid
        if (paidAmount >= due) {
            payment.setStatus(MaintenancePayment.PaymentStatus.PAID);
        } else if (paidAmount > 0) {
            payment.setStatus(MaintenancePayment.PaymentStatus.PARTIAL);
        } else {
            // paidAmount is 0 — keep as UNPAID, don't save
            throw new RuntimeException("Paid amount must be greater than 0");
        }

        return payment;
    }

    // ── Mark as paid by admin (for their own flat) ────────
    @Transactional
    public MaintenancePayment markPaidByAdmin(String flatNo, int month, int year,
                                              MarkPaymentRequest req) {
        MaintenancePayment payment = paymentRepository
                .findByFlatNoAndMonthAndYear(flatNo, month, year)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if (payment.getStatus() == MaintenancePayment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment already fully paid");
        }

        MaintenancePayment saved = paymentRepository.save(applyPayment(payment, req));
        wsPublisher.paymentUpdated(flatNo, month, year);
        return saved;
    }

    // ── Mark as paid (resident action) ────────────────────
    @Transactional
    public MaintenancePayment markPaid(String flatNo, int month, int year,
                                       MarkPaymentRequest req, String callerPhone) {
        Flat flat = flatRepository.findById(flatNo)
                .orElseThrow(() -> new RuntimeException("Flat not found"));

        String payerPhone = flat.getPayerPhone();
        if (!callerPhone.equals(payerPhone) && !callerPhone.equals(flat.getOwnerPhone())) {
            throw new RuntimeException("You are not authorised to mark payment for this flat");
        }

        MaintenancePayment payment = paymentRepository
                .findByFlatNoAndMonthAndYear(flatNo, month, year)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if (payment.getStatus() == MaintenancePayment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment already fully paid");
        }

        MaintenancePayment saved = paymentRepository.save(applyPayment(payment, req));
        wsPublisher.paymentUpdated(flatNo, month, year);
        return saved;
    }

    // ── Admin override: mark PARTIAL as PAID (waive balance) ─
    @Transactional
    public MaintenancePayment waiveBalance(String flatNo, int month, int year) {
        MaintenancePayment payment = paymentRepository
                .findByFlatNoAndMonthAndYear(flatNo, month, year)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if (payment.getStatus() != MaintenancePayment.PaymentStatus.PARTIAL) {
            throw new RuntimeException("Payment is not in PARTIAL status");
        }

        payment.setStatus(MaintenancePayment.PaymentStatus.PAID);
        payment.setUpdatedAt(LocalDateTime.now());
        MaintenancePayment saved = paymentRepository.save(payment);
        wsPublisher.paymentUpdated(flatNo, month, year);
        return saved;
    }

    // ── Undo payment ──────────────────────────────────────
    public MaintenancePayment markUnpaid(String flatNo, int month, int year) {
        MaintenancePayment payment = paymentRepository
                .findByFlatNoAndMonthAndYear(flatNo, month, year)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
        payment.setStatus(MaintenancePayment.PaymentStatus.UNPAID);
        payment.setPaidOn(null);
        payment.setPaidAmount(null);
        payment.setPaymentMode(null);
        payment.setTransactionRef(null);
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    // ── Auto-generate dues for all flats ──────────────────
    @Transactional
    public int generateMonthlyDues(int month, int year) {
        List<Flat> flats = flatRepository.findByIsActiveTrue();
        int count = 0;
        for (Flat flat : flats) {
            if (flat.getFloor() == 0) continue;
            boolean exists = paymentRepository.existsByFlatNoAndMonthAndYear(
                    flat.getFlatNo(), month, year);
            int flatAmount = flat.getMaintenanceAmount() != null
                    ? flat.getMaintenanceAmount() : monthlyAmount;
            if (!exists) {
                paymentRepository.save(MaintenancePayment.builder()
                        .flatNo(flat.getFlatNo())
                        .month(month)
                        .year(year)
                        .amount(flatAmount)
                        .status(MaintenancePayment.PaymentStatus.UNPAID)
                        .payerName(flat.getPayerName())
                        .payerPhone(flat.getPayerPhone())
                        .payerRole(flat.getOwnerType() == Flat.OwnerType.RENTED ? "tenant" : "owner")
                        .ownerType(flat.getOwnerType().name())
                        .ownerName(flat.getOwnerName())
                        .ownerPhone(flat.getOwnerPhone())
                        .build());
                count++;
            }
        }
        return count;
    }

    // ── Send reminders ────────────────────────────────────
    public Map<String, Object> sendReminders(int month, int year) {
        // Include both UNPAID and PARTIAL in reminders
        List<MaintenancePayment> unpaid = paymentRepository
                .findUnpaidByMonthAndYear(month, year);

        String monthLabel = getMonthLabel(month, year);
        int sent = 0, skipped = 0;

        for (MaintenancePayment p : unpaid) {
            String email = null;
            List<User> owners = userRepository.findByFlatNo(p.getFlatNo());
            User ownerUser = owners.stream()
                    .filter(u -> u.getIdentifier() != null
                            && !u.getIdentifier().endsWith("_tenant"))
                    .findFirst().orElse(null);

            if ("RENTED".equals(p.getOwnerType())) {
                User tenantUser = owners.stream()
                        .filter(u -> u.getIdentifier() != null
                                && u.getIdentifier().endsWith("_tenant"))
                        .findFirst().orElse(null);
                if (tenantUser != null && tenantUser.getEmail() != null
                        && !tenantUser.getEmail().isBlank()) {
                    email = tenantUser.getEmail();
                }
            }

            if (email == null && ownerUser != null
                    && ownerUser.getEmail() != null
                    && !ownerUser.getEmail().isBlank()) {
                email = ownerUser.getEmail();
            }

            if (email == null) { skipped++; continue; }

            try {
                // For partial — remind of balance only
                int reminderAmount = p.getStatus() == MaintenancePayment.PaymentStatus.PARTIAL
                        ? p.getBalance()
                        : p.getAmount();
                emailService.sendMaintenanceReminder(
                        email, p.getFlatNo(), p.getPayerName(), monthLabel, reminderAmount);
                sent++;
            } catch (Exception e) {
                log.error("Reminder failed for flat {}: {}", p.getFlatNo(), e.getMessage());
                skipped++;
            }
        }

        return Map.of(
                "message", sent + " reminder emails sent, " + skipped + " skipped",
                "sent", sent, "skipped", skipped
        );
    }

    // ── Summary ───────────────────────────────────────────
    public Map<String, Object> getMonthSummary(int month, int year) {
        List<MaintenancePayment> payments = paymentRepository.findByMonthAndYear(month, year);
        long paid    = payments.stream().filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.PAID).count();
        long partial = payments.stream().filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.PARTIAL).count();
        long unpaid  = payments.stream().filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.UNPAID).count();

        int collected = payments.stream()
                .filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.PAID
                        || p.getStatus() == MaintenancePayment.PaymentStatus.PARTIAL)
                .mapToInt(p -> p.getPaidAmount() != null ? p.getPaidAmount() : 0)
                .sum();

        int pending = payments.stream()
                .filter(p -> p.getStatus() != MaintenancePayment.PaymentStatus.PAID)
                .mapToInt(p -> p.getStatus() == MaintenancePayment.PaymentStatus.PARTIAL
                        ? p.getBalance()
                        : (p.getAmount() != null ? p.getAmount() : monthlyAmount))
                .sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("month",         month);
        summary.put("year",          year);
        summary.put("paid",          paid);
        summary.put("partial",       partial);
        summary.put("unpaid",        unpaid);
        summary.put("total",         payments.size());
        summary.put("collected",     collected);
        summary.put("pending",       pending);
        summary.put("monthlyAmount", monthlyAmount);
        return summary;
    }

    private String getMonthLabel(int month, int year) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"};
        return months[month - 1] + " " + year;
    }
}