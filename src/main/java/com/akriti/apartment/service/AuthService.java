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
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private FlatRepository flatRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private WhatsAppService whatsAppService;

    @Value("${app.admin.username}") private String adminUsername;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.admin.name}")     private String adminName;
    @Value("${app.admin.phone}")    private String adminPhone;

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

        // Send via WhatsApp
        whatsAppService.sendOtp(phone, otp);

        return MessageResponse.builder()
            .message("OTP sent to WhatsApp +91" + phone)
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
}
