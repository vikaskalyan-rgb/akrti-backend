package com.akriti.apartment.controller;

import com.akriti.apartment.dto.*;
import com.akriti.apartment.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    // All complaints — admin sees all, resident filtered in service/frontend
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @GetMapping("/counts")
    public ResponseEntity<?> getCounts() {
        return ResponseEntity.ok(complaintService.getComplaintCounts());
    }

    @GetMapping("/flat/{flatNo}")
    public ResponseEntity<?> getByFlat(@PathVariable String flatNo) {
        return ResponseEntity.ok(complaintService.getComplaintsByFlat(flatNo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(complaintService.getComplaint(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Residents raise complaints
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ComplaintRequest req, Authentication auth) {
        try {
            return ResponseEntity.ok(complaintService.createComplaint(req, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    // Admin updates status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody UpdateComplaintRequest req) {
        try {
            return ResponseEntity.ok(complaintService.updateStatus(id, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }

    // Admin deletes complaint
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        complaintService.getComplaint(id); // validate exists
        return ResponseEntity.ok(
            MessageResponse.builder().message("Deleted").success(true).build());
    }
}
