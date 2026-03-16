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

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ADMIN, OWNER, TENANT

    @Column(name = "flat_no", length = 10)
    private String flatNo;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // OTP fields (transient storage — cleared after verify)
    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

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
