package com.akriti.apartment.controller;

import com.akriti.apartment.dto.*;
import com.akriti.apartment.service.FlatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flats")
public class FlatController {

    @Autowired
    private FlatService flatService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(flatService.getAll());
    }

    @GetMapping("/{flatNo}")
    public ResponseEntity<?> getOne(@PathVariable String flatNo) {
        try {
            return ResponseEntity.ok(flatService.getFlat(flatNo));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Admin can update flat details (e.g. new tenant moved in)
    @PutMapping("/{flatNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String flatNo,
                                    @RequestBody FlatRequest req) {
        try {
            return ResponseEntity.ok(flatService.update(flatNo, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(MessageResponse.builder().message(e.getMessage()).success(false).build());
        }
    }
}
