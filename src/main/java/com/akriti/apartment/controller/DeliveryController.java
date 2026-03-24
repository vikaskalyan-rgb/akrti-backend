package com.akriti.apartment.controller;

import ch.qos.logback.classic.Logger;
import com.akriti.apartment.entity.Delivery;
import com.akriti.apartment.entity.User;
import com.akriti.apartment.repository.DeliveryRepository;
import com.akriti.apartment.repository.UserRepository;
import com.akriti.apartment.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

import static com.akriti.apartment.service.FlatService.log;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired private DeliveryRepository repo;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;

    // All roles — view
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(repo.findAllByOrderByLoggedAtDesc());
    }

    @GetMapping("/flat/{flatNo}")
    public ResponseEntity<?> getByFlat(@PathVariable String flatNo) {
        return ResponseEntity.ok(repo.findByFlatNoOrderByLoggedAtDesc(flatNo));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        return ResponseEntity.ok(repo.findByStatusOrderByLoggedAtDesc("PENDING"));
    }
    // Resident marks collected — or admin/SUP
    @PatchMapping("/{id}/collect")
    public ResponseEntity<?> collect(@PathVariable Long id) {
        return repo.findById(id).map(d -> {
            d.setStatus("COLLECTED");
            d.setCollectedAt(LocalDateTime.now());
            return ResponseEntity.ok(repo.save(d));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<?> log(@RequestBody Delivery delivery) {
        delivery.setLoggedAt(LocalDateTime.now());
        delivery.setStatus("PENDING");
        Delivery saved = repo.save(delivery);

        // Send email to resident if they have one
        try {
            List<User> users = userRepository.findByFlatNo(delivery.getFlatNo());
            users.stream()
                    .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                    .findFirst()
                    .ifPresent(u -> emailService.sendDeliveryNotification(
                            u.getEmail(),
                            delivery.getFlatNo(),
                            delivery.getCourierName(),
                            delivery.getDescription()
                    ));
        } catch (Exception e) {
            log.warn("Could not send delivery email: {}", e.getMessage());
        }

        return ResponseEntity.ok(saved);
    }
}