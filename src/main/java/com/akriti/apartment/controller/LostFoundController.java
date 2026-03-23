package com.akriti.apartment.controller;

import com.akriti.apartment.entity.LostFound;
import com.akriti.apartment.repository.LostFoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/lost-found")
public class LostFoundController {

    @Autowired private LostFoundRepository repo;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(repo.findAllByOrderByCreatedAtDesc());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LostFound item) {
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setStatus("OPEN");
        return ResponseEntity.ok(repo.save(item));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<?> resolve(@PathVariable Long id) {
        return repo.findById(id).map(item -> {
            item.setStatus("RETURNED");
            item.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(repo.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}