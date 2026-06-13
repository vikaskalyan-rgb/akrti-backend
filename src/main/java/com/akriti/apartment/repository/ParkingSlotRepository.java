// ════════════════════════════════════════════════════════════
//  FILE 1: ParkingSlotRepository.java
//  Place in: com.akriti.apartment.repository
// ════════════════════════════════════════════════════════════
package com.akriti.apartment.repository;

import com.akriti.apartment.entity.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    List<ParkingSlot> findByAssignedFlat(String assignedFlat);
    Optional<ParkingSlot> findByLabel(String label);
}


