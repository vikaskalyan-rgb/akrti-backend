package com.akriti.apartment.controller;

import com.akriti.apartment.entity.Delivery;
import com.akriti.apartment.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired private DeliveryRepository repo;

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

    // SUP / ADMIN — log delivery
    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<?> log(@RequestBody Delivery delivery) {
        delivery.setLoggedAt(LocalDateTime.now());
        delivery.setStatus("PENDING");
        return ResponseEntity.ok(repo.save(delivery));
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
}