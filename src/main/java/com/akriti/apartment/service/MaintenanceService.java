package com.akriti.apartment.service;

import com.akriti.apartment.dto.MarkPaymentRequest;
import com.akriti.apartment.entity.*;
import com.akriti.apartment.repository.*;
import com.akriti.apartment.websocket.WebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MaintenanceService {

    @Autowired private MaintenancePaymentRepository paymentRepository;
    @Autowired private FlatRepository flatRepository;
    @Autowired private WhatsAppService whatsAppService;
    @Autowired private WebSocketPublisher wsPublisher;

    @Value("${app.monthly.maintenance:4200}")
    private int monthlyAmount;

    // ── Get all payments for a month ──────────────────────
    public List<MaintenancePayment> getMonthPayments(int month, int year) {
        return paymentRepository.findByMonthAndYear(month, year);
    }

    // ── Get payments for a specific flat ─────────────────
    public List<MaintenancePayment> getFlatPayments(String flatNo) {
        return paymentRepository.findByFlatNoOrderByYearDescMonthDesc(flatNo);
    }

    // ── Get single payment ────────────────────────────────
    public MaintenancePayment getPayment(String flatNo, int month, int year) {
        return paymentRepository.findByFlatNoAndMonthAndYear(flatNo, month, year)
            .orElseThrow(() -> new RuntimeException("Payment record not found"));
    }

    // ── Mark as paid by admin (for their own flat, no phone check) ──
    @Transactional
    public MaintenancePayment markPaidByAdmin(String flatNo, int month, int year,
                                              MarkPaymentRequest req) {
        MaintenancePayment payment = paymentRepository
                .findByFlatNoAndMonthAndYear(flatNo, month, year)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if (payment.getStatus() == MaintenancePayment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment already marked as paid");
        }

        MaintenancePayment.PaymentMode mode = MaintenancePayment.PaymentMode.valueOf(
                req.getPaymentMode().toUpperCase().replace(" ", "_")
        );

        payment.setStatus(MaintenancePayment.PaymentStatus.PAID);
        payment.setAmount(monthlyAmount);
        payment.setPaidOn(LocalDate.now());
        payment.setPaymentMode(mode);
        payment.setTransactionRef(req.getTransactionRef());
        payment.setMarkedByResident(true);
        payment.setUpdatedAt(LocalDateTime.now());

        MaintenancePayment saved = paymentRepository.save(payment);
        wsPublisher.paymentUpdated(flatNo, month, year);
        return saved;
    }

    // ── Mark as paid (resident action) ────────────────────
    @Transactional
    public MaintenancePayment markPaid(String flatNo, int month, int year,
                                       MarkPaymentRequest req, String callerPhone) {
        // Verify caller is the payer for this flat
        Flat flat = flatRepository.findById(flatNo)
            .orElseThrow(() -> new RuntimeException("Flat not found"));

        String payerPhone = flat.getPayerPhone();
        if (!callerPhone.equals(payerPhone)) {
            // Also allow owner to pay for their own vacant/rented flat
            if (!callerPhone.equals(flat.getOwnerPhone())) {
                throw new RuntimeException("You are not authorized to mark payment for this flat");
            }
        }

        MaintenancePayment payment = paymentRepository
            .findByFlatNoAndMonthAndYear(flatNo, month, year)
            .orElseThrow(() -> new RuntimeException("Payment record not found"));

        if (payment.getStatus() == MaintenancePayment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment already marked as paid");
        }

        MaintenancePayment.PaymentMode mode = MaintenancePayment.PaymentMode.valueOf(
            req.getPaymentMode().toUpperCase().replace(" ", "_")
        );

        payment.setStatus(MaintenancePayment.PaymentStatus.PAID);
        payment.setAmount(monthlyAmount);
        payment.setPaidOn(LocalDate.now());
        payment.setPaymentMode(mode);
        payment.setTransactionRef(req.getTransactionRef());
        payment.setMarkedByResident(true);
        payment.setUpdatedAt(LocalDateTime.now());

        MaintenancePayment saved = paymentRepository.save(payment);

        // Push WebSocket event so admin dashboard updates instantly
        wsPublisher.paymentUpdated(flatNo, month, year);

        return saved;
    }

    // ── Auto-generate dues for all flats (called by scheduler) ──
    @Transactional
    public int generateMonthlyDues(int month, int year) {
        List<Flat> flats = flatRepository.findByIsActiveTrue();
        int count = 0;

        for (Flat flat : flats) {
            if (flat.getFloor() == 0) continue; // skip ground floor utility units

            boolean exists = paymentRepository.existsByFlatNoAndMonthAndYear(
                flat.getFlatNo(), month, year);

            if (!exists) {
                MaintenancePayment payment = MaintenancePayment.builder()
                    .flatNo(flat.getFlatNo())
                    .month(month)
                    .year(year)
                    .amount(monthlyAmount)
                    .status(MaintenancePayment.PaymentStatus.UNPAID)
                    .payerName(flat.getPayerName())
                    .payerPhone(flat.getPayerPhone())
                    .payerRole(flat.getOwnerType() == Flat.OwnerType.RENTED ? "tenant" : "owner")
                    .ownerType(flat.getOwnerType().name())
                    .ownerName(flat.getOwnerName())
                    .ownerPhone(flat.getOwnerPhone())
                    .build();

                paymentRepository.save(payment);
                count++;
            }
        }
        return count;
    }

    // ── Send WhatsApp reminders to all defaulters ─────────
    public int sendReminders(int month, int year) {
        List<MaintenancePayment> unpaid = paymentRepository
            .findUnpaidByMonthAndYear(month, year);

        String monthLabel = getMonthLabel(month, year);
        int count = 0;

        for (MaintenancePayment p : unpaid) {
            if (p.getPayerPhone() != null) {
                whatsAppService.sendMaintenanceReminder(
                    p.getPayerPhone(), p.getPayerName(),
                    p.getFlatNo(), monthlyAmount, monthLabel
                );
                count++;
            }
        }
        return count;
    }

    // ── Dashboard summary ─────────────────────────────────
    public Map<String, Object> getMonthSummary(int month, int year) {
        List<MaintenancePayment> payments = paymentRepository.findByMonthAndYear(month, year);
        long paid    = payments.stream().filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.PAID).count();
        long unpaid  = payments.stream().filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.UNPAID).count();
        int collected = payments.stream()
            .filter(p -> p.getStatus() == MaintenancePayment.PaymentStatus.PAID)
            .mapToInt(MaintenancePayment::getAmount).sum();
        int pending  = (int)(unpaid * monthlyAmount);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("month", month);
        summary.put("year", year);
        summary.put("paid", paid);
        summary.put("unpaid", unpaid);
        summary.put("total", payments.size());
        summary.put("collected", collected);
        summary.put("pending", pending);
        summary.put("monthlyAmount", monthlyAmount);
        return summary;
    }

    private String getMonthLabel(int month, int year) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        return months[month - 1] + " " + year;
    }
}
