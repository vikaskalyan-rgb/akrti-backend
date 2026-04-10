package com.akriti.apartment.repository;
import com.akriti.apartment.entity.NearbyPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NearbyPlaceRepository extends JpaRepository<NearbyPlace, Long> {
    List<NearbyPlace> findByIsActiveTrueOrderByCategoryAscNameAsc();
    
}