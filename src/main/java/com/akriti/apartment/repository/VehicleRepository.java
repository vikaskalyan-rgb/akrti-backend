// ════════════════════════════════════════════════════════════
//  FILE 2: VehicleRepository.java
//  Place in: com.akriti.apartment.repository
// ════════════════════════════════════════════════════════════
package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findBySlotId(Long slotId);
    List<Vehicle> findByFlatNo(String flatNo);

    @Transactional
    void deleteByFlatNoAndIsTenant(String flatNo, Boolean isTenant);

    @Transactional
    void deleteBySlotId(Long slotId);
}