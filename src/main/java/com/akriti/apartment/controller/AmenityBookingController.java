package com.akriti.apartment.controller;

import com.akriti.apartment.entity.AmenityBooking;
import com.akriti.apartment.repository.AmenityBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/amenity-bookings")
public class AmenityBookingController {

    @Autowired private AmenityBookingRepository repo;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(repo.findAllByOrderByBookingDateDescCreatedAtDesc());
    }

    @GetMapping("/flat/{flatNo}")
    public ResponseEntity<?> getByFlat(@PathVariable String flatNo) {
        return ResponseEntity.ok(repo.findByFlatNoOrderByBookingDateDesc(flatNo));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<?> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(repo.findByBookingDateOrderByStartTimeAsc(date));
    }

    @PostMapping
    public ResponseEntity<?> book(@RequestBody AmenityBooking booking) {
        // Check if already booked for that date
        boolean conflict = repo.existsByAmenityAndBookingDateAndStatusNot(
                booking.getAmenity(), booking.getBookingDate(), "REJECTED");
        if (conflict) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Community Hall is already booked for this date",
                            "success", false));
        }
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStatus("PENDING");
        return ResponseEntity.ok(repo.save(booking));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String,String> body) {
        return repo.findById(id).map(b -> {
            b.setStatus("APPROVED");
            if (body != null && body.get("adminNote") != null)
                b.setAdminNote(body.get("adminNote"));
            return ResponseEntity.ok(repo.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @RequestBody(required = false) Map<String,String> body) {
        return repo.findById(id).map(b -> {
            b.setStatus("REJECTED");
            if (body != null && body.get("adminNote") != null)
                b.setAdminNote(body.get("adminNote"));
            return ResponseEntity.ok(repo.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}