package com.akriti.apartment.controller;

import com.akriti.apartment.entity.AmenityBooking;
import com.akriti.apartment.entity.User;
import com.akriti.apartment.repository.AmenityBookingRepository;
import com.akriti.apartment.repository.UserRepository;
import com.akriti.apartment.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.akriti.apartment.service.FlatService.log;

@RestController
@RequestMapping("/api/amenity-bookings")
public class AmenityBookingController {

    @Autowired private AmenityBookingRepository repo;
    @Autowired private EmailService emailService;
    @Autowired private UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(AmenityBookingController.class);


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



    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String,String> body) {
        return repo.findById(id).map(b -> {
            b.setStatus("APPROVED");
            if (body != null && body.get("adminNote") != null)
                b.setAdminNote(body.get("adminNote"));
            AmenityBooking saved = repo.save(b);

            // Notify resident
            try {
                List<User> users = userRepository.findByFlatNo(b.getFlatNo());
                users.stream()
                        .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                        .findFirst()
                        .ifPresent(u -> emailService.sendBookingStatusToResident(
                                u.getEmail(), b.getFlatNo(), b.getPurpose(),
                                b.getBookingDate().toString(), "APPROVED",
                                body != null ? body.get("adminNote") : null));
            } catch (Exception e) {
                log.warn("Could not send approval email: {}", e.getMessage());
            }

            return ResponseEntity.ok(saved);
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
            AmenityBooking saved = repo.save(b);

            // Notify resident
            try {
                List<User> users = userRepository.findByFlatNo(b.getFlatNo());
                users.stream()
                        .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                        .findFirst()
                        .ifPresent(u -> emailService.sendBookingStatusToResident(
                                u.getEmail(), b.getFlatNo(), b.getPurpose(),
                                b.getBookingDate().toString(), "REJECTED",
                                body != null ? body.get("adminNote") : null));
            } catch (Exception e) {
                log.warn("Could not send rejection email: {}", e.getMessage());
            }

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping
    public ResponseEntity<?> book(@RequestBody AmenityBooking booking) {
        boolean conflict = repo.existsByAmenityAndBookingDateAndStatusNot(
                booking.getAmenity(), booking.getBookingDate(), "REJECTED");
        if (conflict) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Community Hall is already booked for this date",
                            "success", false));
        }
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStatus("PENDING");
        AmenityBooking saved = repo.save(booking);

        // Notify all admins by email
        try {
            emailService.sendBookingRequestToAdmins(
                    saved.getFlatNo(),
                    saved.getBookedBy(),
                    saved.getBookingDate().toString(),
                    saved.getStartTime(),
                    saved.getEndTime(),
                    saved.getPurpose()
            );
        } catch (Exception e) {
            log.warn("Could not send booking notification: {}", e.getMessage());
        }

        return ResponseEntity.ok(saved);
    }
}