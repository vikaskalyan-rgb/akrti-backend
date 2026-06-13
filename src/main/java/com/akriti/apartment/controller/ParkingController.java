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

    /** PUT /api/parking/slots/{id} — admin updates slot (assign flat, move, capacity) */
    @PutMapping("/slots/{id}")
    public ResponseEntity<?> updateSlot(@PathVariable Long id, @RequestBody ParkingSlot body) {
        return slotRepo.findById(id).map(slot -> {
            if (body.getLabel() != null)        slot.setLabel(body.getLabel());
            if (body.getPosX() != null)         slot.setPosX(body.getPosX());
            if (body.getPosZ() != null)         slot.setPosZ(body.getPosZ());
            if (body.getRotation() != null)     slot.setRotation(body.getRotation());
            if (body.getCarCapacity() != null)  slot.setCarCapacity(body.getCarCapacity());
            if (body.getBikeCapacity() != null) slot.setBikeCapacity(body.getBikeCapacity());
            // assignedFlat: allow explicit null to unassign
            slot.setAssignedFlat(body.getAssignedFlat());

            ParkingSlot saved = slotRepo.save(slot);

            // If slot got unassigned, clear its vehicles
            if (body.getAssignedFlat() == null || body.getAssignedFlat().isBlank()) {
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

    /** POST /api/parking/slots/bulk — admin saves many slot positions at once */
    @PostMapping("/slots/bulk")
    public ResponseEntity<?> bulkUpdate(@RequestBody List<ParkingSlot> slots) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ParkingSlot body : slots) {
            if (body.getId() != null) {
                slotRepo.findById(body.getId()).ifPresent(slot -> {
                    slot.setPosX(body.getPosX());
                    slot.setPosZ(body.getPosZ());
                    slot.setRotation(body.getRotation());
                    slotRepo.save(slot);
                });
            }
        }
        slotRepo.findAll().forEach(s ->
                result.add(slotToMap(s, vehicleRepo.findBySlotId(s.getId()))));
        return ResponseEntity.ok(result);
    }

    // ════════════════════════════════════════════════════════
    //  VEHICLES
    // ════════════════════════════════════════════════════════

    /**
     * POST /api/parking/vehicles — add a vehicle to a slot.
     * Enforces: slot must be assigned to the vehicle's flat,
     * and capacity (cars/bikes) must not be exceeded.
     */
    @PostMapping("/vehicles")
    public ResponseEntity<?> addVehicle(@RequestBody Vehicle v) {
        ParkingSlot slot = slotRepo.findById(v.getSlotId()).orElse(null);
        if (slot == null) return ResponseEntity.badRequest().body(error("Slot not found"));

        // Slot must belong to this flat
        if (slot.getAssignedFlat() == null || !slot.getAssignedFlat().equalsIgnoreCase(v.getFlatNo())) {
            return ResponseEntity.badRequest().body(error("This slot is not assigned to your flat"));
        }

        // Capacity check
        List<Vehicle> existing = vehicleRepo.findBySlotId(slot.getId());
        String t = v.getType() == null ? "" : v.getType().toUpperCase();
        boolean isBikeType = t.equals("BIKE") || t.equals("SCOOTER") || t.equals("CYCLE");
        long cars  = existing.stream().filter(e -> {
            String et = e.getType().toUpperCase();
            return et.equals("CAR") || et.equals("TEMPO");
        }).count();
        long bikes = existing.stream().filter(e -> {
            String et = e.getType().toUpperCase();
            return et.equals("BIKE") || et.equals("SCOOTER") || et.equals("CYCLE");
        }).count();

        if (isBikeType && bikes >= slot.getBikeCapacity()) {
            return ResponseEntity.badRequest().body(error("No bike space left in this slot"));
        }
        if (!isBikeType && cars >= slot.getCarCapacity()) {
            return ResponseEntity.badRequest().body(error("No car space left in this slot"));
        }

        v.setId(null);
        v.setType(t);
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
     * (Wire this into your flat-management tenant-removal flow.)
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
        m.put("posX", s.getPosX());
        m.put("posZ", s.getPosZ());
        m.put("rotation", s.getRotation());
        m.put("carCapacity", s.getCarCapacity());
        m.put("bikeCapacity", s.getBikeCapacity());
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