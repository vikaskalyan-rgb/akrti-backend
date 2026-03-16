package com.akriti.apartment.controller;

import com.akriti.apartment.dto.*;
import com.akriti.apartment.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "false") boolean todayOnly,
                                    @RequestParam(required = false) String flatNo) {
        if (flatNo != null) return ResponseEntity.ok(visitorService.getByFlat(flatNo));
        if (todayOnly)      return ResponseEntity.ok(visitorService.getToday());
        return ResponseEntity.ok(visitorService.getAll());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(visitorService.getStats());
    }

    // Admin logs entry
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> logEntry(@RequestBody VisitorRequest req) {
        try {
            return ResponseEntity.ok(visitorService.logEntry(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    // Admin checks out visitor
    @PatchMapping("/{id}/checkout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkout(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(visitorService.checkout(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }
}
