package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_payments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"flat_no", "month", "year"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenancePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flat_no", nullable = false, length = 10)
    private String flatNo;

    @Column(nullable = false)
    private Integer month; // 1-12

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    @Builder.Default
    private Integer amount = 4200;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Column(name = "paid_on")
    private LocalDate paidOn;

    @Column(name = "payment_mode", length = 30)
    private PaymentMode paymentMode;

    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "payer_phone", length = 15)
    private String payerPhone;

    @Column(name = "payer_role", length = 10)
    private String payerRole; // owner / tenant

    @Column(name = "owner_type", length = 20)
    private String ownerType;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_phone", length = 15)
    private String ownerPhone;

    @Column(name = "transaction_ref")
    private String transactionRef;

    @Column(name = "marked_by_resident")
    @Builder.Default
    private Boolean markedByResident = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum PaymentStatus {
        PAID, UNPAID
    }

    public enum PaymentMode {
        UPI, CASH, BANK_TRANSFER, CHEQUE
    }
}
