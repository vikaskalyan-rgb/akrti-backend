package com.akriti.apartment.controller;

import com.akriti.apartment.dto.MessageResponse;
import com.akriti.apartment.service.WatchmanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/watchman")
public class WatchmanController {

    @Autowired private WatchmanService watchmanService;

    // All roles — view status and summary
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(watchmanService.getCurrentStatus());
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(watchmanService.getSummary(days));
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(watchmanService.getByDate(date));
    }

    // Supervisor only — log whistle
    @PostMapping("/log")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<?> log() {
        try {
            return ResponseEntity.ok(watchmanService.logNow());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.builder()
                            .message(e.getMessage()).success(false).build());
        }
    }
}