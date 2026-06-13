package com.akriti.apartment.controller;

import com.akriti.apartment.entity.ParkingSlot;
import com.akriti.apartment.entity.Vehicle;
import com.akriti.apartment.repository.ParkingSlotRepository;
import com.akriti.apartment.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(origins = "*")
public class ParkingController {

    @Autowired private ParkingSlotRepository slotRepo;
    @Autowired private VehicleRepository     vehicleRepo;

    // ════════════════════════════════════════════════════════
    //  SLOTS
    // ════════════════════════════════════════════════════════

    /** GET /api/parking/slots — all slots, each with its vehicles */
    @GetMapping("/slots")
    public ResponseEntity<?> getAllSlots() {
        List<ParkingSlot> slots = slotRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ParkingSlot s : slots) {
            result.add(slotToMap(s, vehicleRepo.findBySlotId(s.getId())));
        }
        return ResponseEntity.ok(result);
    }

    /** POST /api/parking/slots — admin creates a new slot */
    @PostMapping("/slots")
    public ResponseEntity<?> createSlot(@RequestBody ParkingSlot slot) {
        slot.setId(null);
        ParkingSlot saved = slotRepo.save(slot);
        return ResponseEntity.ok(slotToMap(saved, List.of()));
    }

    /** PUT /api/parking/slots/{id} — admin assigns a flat (or unassigns) */
    @PutMapping("/slots/{id}")
    public ResponseEntity<?> updateSlot(@PathVariable Long id, @RequestBody ParkingSlot body) {
        return slotRepo.findById(id).map(slot -> {
            if (body.getLabel() != null) slot.setLabel(body.getLabel());

            // assignedFlat: allow explicit null/blank to unassign
            String newFlat = body.getAssignedFlat();
            boolean unassigning = (newFlat == null || newFlat.isBlank());
            slot.setAssignedFlat(unassigning ? null : newFlat);

            ParkingSlot saved = slotRepo.save(slot);

            // clearing the flat clears the slot's vehicles
            if (unassigning) {
                vehicleRepo.deleteBySlotId(saved.getId());
            }
            return ResponseEntity.ok(slotToMap(saved, vehicleRepo.findBySlotId(saved.getId())));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/parking/slots/{id} — admin removes a slot */
    @DeleteMapping("/slots/{id}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long id) {
        if (!slotRepo.existsById(id)) return ResponseEntity.notFound().build();
        slotRepo.deleteById(id);   // vehicles cascade-deleted
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("deleted", true);
        r.put("id", id);
        return ResponseEntity.ok(r);
    }

    // ════════════════════════════════════════════════════════
    //  VEHICLES
    // ════════════════════════════════════════════════════════

    /**
     * POST /api/parking/vehicles — add a vehicle to a slot.
     * Rule: the slot must be assigned to the vehicle's flat.
     * No capacity limit — a flat can add as many vehicles as they have.
     */
    @PostMapping("/vehicles")
    public ResponseEntity<?> addVehicle(@RequestBody Vehicle v) {
        ParkingSlot slot = slotRepo.findById(v.getSlotId()).orElse(null);
        if (slot == null) return ResponseEntity.badRequest().body(error("Slot not found"));

        if (slot.getAssignedFlat() == null ||
                !slot.getAssignedFlat().equalsIgnoreCase(v.getFlatNo())) {
            return ResponseEntity.badRequest().body(error("This slot is not assigned to your flat"));
        }

        v.setId(null);
        v.setType(v.getType() == null ? "CAR" : v.getType().toUpperCase());
        Vehicle saved = vehicleRepo.save(v);
        return ResponseEntity.ok(saved);
    }

    /** DELETE /api/parking/vehicles/{id} — remove a vehicle */
    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        if (!vehicleRepo.existsById(id)) return ResponseEntity.notFound().build();
        vehicleRepo.deleteById(id);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("deleted", true);
        r.put("id", id);
        return ResponseEntity.ok(r);
    }

    /** GET /api/parking/vehicles/flat/{flatNo} — vehicles for a flat */
    @GetMapping("/vehicles/flat/{flatNo}")
    public ResponseEntity<?> vehiclesByFlat(@PathVariable String flatNo) {
        return ResponseEntity.ok(vehicleRepo.findByFlatNo(flatNo));
    }

    /**
     * DELETE /api/parking/vehicles/tenant/{flatNo}
     * Called when a tenant leaves — removes only tenant-added vehicles.
     */
    @DeleteMapping("/vehicles/tenant/{flatNo}")
    public ResponseEntity<?> removeTenantVehicles(@PathVariable String flatNo) {
        vehicleRepo.deleteByFlatNoAndIsTenant(flatNo, true);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("removed", true);
        r.put("flatNo", flatNo);
        return ResponseEntity.ok(r);
    }

    // ── helpers ──
    private Map<String, Object> slotToMap(ParkingSlot s, List<Vehicle> vehicles) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("label", s.getLabel());
        m.put("assignedFlat", s.getAssignedFlat());
        m.put("vehicles", vehicles);
        return m;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", msg);
        return m;
    }
}