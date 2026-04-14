package com.akriti.apartment.controller;

import com.akriti.apartment.entity.CommunityEvent;
import com.akriti.apartment.repository.CommunityEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/classes-events")
public class ClassesEventsController {

    @Autowired private CommunityEventRepository eventRepo;

    // ── GET all events ────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(eventRepo.findAllByOrderByCreatedAtDesc());
    }

    // ── POST create event ─────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        CommunityEvent event = CommunityEvent.builder()
                .category(body.getOrDefault("category", "OTHER"))
                .title(body.get("title"))
                .description(body.get("description"))
                .eventDate(body.get("eventDate") != null && !body.get("eventDate").isBlank()
                        ? LocalDate.parse(body.get("eventDate")) : null)
                .eventTime(body.get("eventTime") != null && !body.get("eventTime").isBlank()
                        ? LocalTime.parse(body.get("eventTime")) : null)
                .location(body.get("location"))
                .contactPhone(body.get("contactPhone"))
                .flatNo(body.get("flatNo"))
                .userName(body.get("userName"))
                .build();
        return ResponseEntity.ok(eventRepo.save(event));
    }

    // ── DELETE event ──────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return eventRepo.findById(id).map(e -> {
            eventRepo.delete(e);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }
}