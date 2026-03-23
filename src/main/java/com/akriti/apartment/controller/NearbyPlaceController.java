package com.akriti.apartment.controller;

import com.akriti.apartment.dto.MessageResponse;
import com.akriti.apartment.entity.NearbyPlace;
import com.akriti.apartment.repository.NearbyPlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/places")
public class NearbyPlaceController {

    @Autowired private NearbyPlaceRepository repo;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(repo.findByIsActiveTrueOrderByCategoryAscNameAsc());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, String> req) {
        NearbyPlace p = NearbyPlace.builder()
                .name(req.get("name"))
                .category(req.get("category"))
                .address(req.get("address"))
                .phone(req.get("phone"))
                .mapsUrl(req.get("mapsUrl"))
                .distance(req.get("distance"))
                .addedBy(req.get("addedBy"))
                .build();
        return ResponseEntity.ok(repo.save(p));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, String> req) {
        return repo.findById(id).map(p -> {
            if (req.get("name")     != null) p.setName(req.get("name"));
            if (req.get("category") != null) p.setCategory(req.get("category"));
            if (req.get("address")  != null) p.setAddress(req.get("address"));
            if (req.get("phone")    != null) p.setPhone(req.get("phone"));
            if (req.get("mapsUrl")  != null) p.setMapsUrl(req.get("mapsUrl"));
            if (req.get("distance") != null) p.setDistance(req.get("distance"));
            return ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repo.findById(id).map(p -> {
            p.setIsActive(false);
            repo.save(p);
            return ResponseEntity.ok(
                    MessageResponse.builder().message("Removed").success(true).build());
        }).orElse(ResponseEntity.notFound().build());
    }
}