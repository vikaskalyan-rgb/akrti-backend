package com.akriti.apartment.service;

import com.akriti.apartment.dto.*;
import com.akriti.apartment.entity.Flat;
import com.akriti.apartment.entity.User;
import com.akriti.apartment.repository.FlatRepository;
import com.akriti.apartment.repository.UserRepository;
import com.akriti.apartment.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private FlatRepository flatRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private OtpService otpService;
    @Autowired private EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}") private String adminUsername;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.admin.name}")     private String adminName;
    @Value("${app.admin.phone}")    private String adminPhone;

    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // ── Admin login ───────────────────────────────────────
    public AuthResponse adminLogin(AdminLoginRequest req) {
        if (!req.getUsername().equals(adminUsername) || !req.getPassword().equals(adminPassword)) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(adminPhone, "ADMIN", null, adminName);
        return AuthResponse.builder()
            .token(token).role("ADMIN").name(adminName).phone(adminPhone).flatNo(null)
            .build();
    }

    // ── Send OTP ──────────────────────────────────────────
    public MessageResponse sendOtp(SendOtpRequest req) {
        String phone = req.getPhone().trim();

        // Check if admin, owner or tenant
        boolean isAdmin  = userRepository.findByPhone(phone)
                .map(u -> u.getRole() == User.Role.ADMIN).orElse(false);
        boolean isOwner  = flatRepository.findByOwnerPhone(phone).isPresent();
        boolean isTenant = flatRepository.findByResidentPhone(phone).isPresent();

        if (!isAdmin && !isOwner && !isTenant) {
            throw new RuntimeException("Phone number not registered in our system");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        log.info("OTP for +91{}: {}", phone, otp); // Always log for dev

        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            String name   = "Resident";
            User.Role role = User.Role.OWNER;
            String flatNo  = null;

            Optional<Flat> ownerFlat = flatRepository.findByOwnerPhone(phone);
            if (ownerFlat.isPresent()) {
                Flat f = ownerFlat.get();
                name   = f.getOwnerName();
                flatNo = f.getFlatNo();
                role   = User.Role.OWNER;
            } else {
                Optional<Flat> tenantFlat = flatRepository.findByResidentPhone(phone);
                if (tenantFlat.isPresent()) {
                    Flat f = tenantFlat.get();
                    name   = f.getResidentName();
                    flatNo = f.getFlatNo();
                    role   = User.Role.TENANT;
                }
            }
            return User.builder()
                    .phone(phone).name(name).role(role).flatNo(flatNo).build();
        });

        user.setOtpCode(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send via sms
        otpService.sendOtp(phone, otp);

        return MessageResponse.builder()
            .message("OTP sent to sms +91" + phone)
            .success(true)
            .build();
    }

    // ── Verify OTP ────────────────────────────────────────
    public AuthResponse verifyOtp(VerifyOtpRequest req) {
        String phone = req.getPhone().trim();
        String otp   = req.getOtp().trim();

        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new RuntimeException("Phone not found"));

        if (!user.isOtpValid(otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Clear OTP after successful verification
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(
            phone,
            user.getRole().name(),
            user.getFlatNo(),
            user.getName()
        );

        return AuthResponse.builder()
            .token(token)
            .role(user.getRole().name())
            .name(user.getName())
            .phone(phone)
            .flatNo(user.getFlatNo())
            .build();
    }

    // ── Flat + Password Login ──────────────────────────────────────
    public AuthResponse loginWithPassword(LoginRequest req) {
        String identifier = req.getIdentifier().trim().toUpperCase()
                .replace("_TENANT", "_tenant"); // normalize

        User user = userRepository.findByIdentifier(req.getIdentifier().trim())
                .orElseThrow(() -> new RuntimeException(
                        "Identifier not registered. Use flat number (e.g. 2H) or flat_tenant (e.g. 4B_tenant)"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(
                user.getPhone() != null ? user.getPhone() : user.getIdentifier(),
                user.getRole().name(),
                user.getFlatNo(),
                user.getName()
        );

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .flatNo(user.getFlatNo())
                .role(user.getRole().name().toLowerCase())
                .phone(user.getPhone())
                .isFirstLogin(user.getFirstLogin())
                .identifier(user.getIdentifier())
                .build();
    }

    // ── Forgot Password ────────────────────────────────────────────
    public void forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByIdentifier(req.getIdentifier().trim())
                .orElseThrow(() -> new RuntimeException("Identifier not registered"));

        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(req.getEmail())) {
            throw new RuntimeException("Email does not match our records");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setPasswordResetOtp(otp);
        user.setPasswordResetOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(req.getEmail(), otp, user.getFlatNo());
    }

    // ── Reset Password ─────────────────────────────────────────────
    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByIdentifier(req.getIdentifier().trim())
                .orElseThrow(() -> new RuntimeException("Identifier not registered"));

        if (user.getPasswordResetOtp() == null
                || !user.getPasswordResetOtp().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getPasswordResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired. Please request a new one.");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordResetOtp(null);
        user.setPasswordResetOtpExpiry(null);
        user.setFirstLogin(false);
        userRepository.save(user);
    }

}
