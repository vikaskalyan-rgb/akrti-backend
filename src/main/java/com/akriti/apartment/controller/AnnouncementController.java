package com.akriti.apartment.controller;

import com.akriti.apartment.dto.*;
import com.akriti.apartment.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // Admin sees all; residents get filtered list
    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return ResponseEntity.ok(announcementService.getAll());
        }
        return ResponseEntity.ok(announcementService.getForResident(auth.getName()));
    }
    @GetMapping("/recipient-count")
    public ResponseEntity<?> getRecipientCount(@RequestParam String audience) {
        return ResponseEntity.ok(Map.of("count", announcementService.getRecipientCount(audience)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody AnnouncementRequest req) {
        try {
            return ResponseEntity.ok(announcementService.create(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    @PatchMapping("/{id}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> togglePin(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.togglePin(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.ok(
            MessageResponse.builder().message("Deleted").success(true).build());
    }
}
