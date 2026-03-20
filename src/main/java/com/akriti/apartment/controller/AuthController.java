package com.akriti.apartment.controller;

import com.akriti.apartment.dto.*;
import com.akriti.apartment.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest req) {
        try {
            return ResponseEntity.ok(authService.adminLogin(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody SendOtpRequest req) {
        try {
            return ResponseEntity.ok(authService.sendOtp(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        try {
            return ResponseEntity.ok(authService.verifyOtp(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }
    // ── Flat + Password Login ──────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(authService.loginWithPassword(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    // ── Forgot Password ────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        try {
            authService.forgotPassword(req);
            return ResponseEntity.ok(
                    MessageResponse.builder()
                            .message("OTP sent to your registered email")
                            .success(true).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    // ── Reset Password ─────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            authService.resetPassword(req);
            return ResponseEntity.ok(
                    MessageResponse.builder()
                            .message("Password reset successfully")
                            .success(true).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    @GetMapping("/api/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "app", "Akriti Adeshwar"));
    }
}
