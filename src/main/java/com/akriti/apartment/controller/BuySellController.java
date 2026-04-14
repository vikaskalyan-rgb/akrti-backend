package com.akriti.apartment.controller;

import com.akriti.apartment.entity.Listing;
import com.akriti.apartment.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/buy-sell")
public class BuySellController {

    @Autowired private ListingRepository listingRepo;

    // ── GET all active listings ───────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(listingRepo.findByIsActiveTrueOrderByCreatedAtDesc());
    }

    // ── POST create listing ───────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Listing listing = Listing.builder()
                .category((String) body.getOrDefault("category", "OTHER"))
                .title((String) body.get("title"))
                .description((String) body.get("description"))
                .price(body.get("price") != null
                        ? ((Number) body.get("price")).longValue() : 0L)
                .flatNo((String) body.get("flatNo"))
                .sellerName((String) body.get("sellerName"))
                .sellerPhone((String) body.get("sellerPhone"))
                .build();
        return ResponseEntity.ok(listingRepo.save(listing));
    }

    // ── DELETE listing (soft delete — sets isActive = false) ─────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return listingRepo.findById(id).map(l -> {
            l.setIsActive(false);
            listingRepo.save(l);
            return ResponseEntity.ok(Map.of("message", "Removed"));
        }).orElse(ResponseEntity.notFound().build());
    }
}