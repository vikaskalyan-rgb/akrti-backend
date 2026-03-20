package com.akriti.apartment.controller;

import com.akriti.apartment.dto.MessageResponse;
import com.akriti.apartment.dto.WorkerRequest;
import com.akriti.apartment.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    @Autowired private WorkerService workerService;

    // All roles can view
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(workerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(workerService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Admin only — create, update, deactivate
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody WorkerRequest req) {
        try {
            return ResponseEntity.ok(workerService.create(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.builder()
                            .message(e.getMessage()).success(false).build());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody WorkerRequest req) {
        try {
            return ResponseEntity.ok(workerService.update(id, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.builder()
                            .message(e.getMessage()).success(false).build());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        try {
            workerService.deactivate(id);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Worker removed").success(true).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.builder()
                            .message(e.getMessage()).success(false).build());
        }
    }
}