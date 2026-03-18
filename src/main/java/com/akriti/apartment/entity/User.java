package com.akriti.apartment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "flat_no")
    private String flatNo;

    @Column(name = "identifier", unique = true)
    private String identifier; // 4B for owner, 4B_tenant for tenant, SUP for supervisor

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "email")
    private String email;

    @Column(name = "password_reset_otp")
    private String passwordResetOtp;

    @Column(name = "password_reset_otp_expiry")
    private LocalDateTime passwordResetOtpExpiry;

    @Column(name = "is_first_login", nullable = false, columnDefinition = "boolean default true")
    private Boolean firstLogin = true;

    public enum Role {
        ADMIN, OWNER, TENANT
    }

    public boolean isOtpValid(String otp) {
        return otpCode != null
                && otpCode.equals(otp)
                && otpExpiresAt != null
                && LocalDateTime.now().isBefore(otpExpiresAt);
    }
}