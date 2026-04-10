package com.akriti.apartment.repository;

import com.akriti.apartment.entity.NearbyPlace;
import com.akriti.apartment.entity.PlaceLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlaceLikeRepository extends JpaRepository<PlaceLike, Long> {
    Optional<PlaceLike> findByPlaceAndFlatNo(NearbyPlace place, String flatNo);
    int countByPlace(NearbyPlace place);
    boolean existsByPlaceAndFlatNo(NearbyPlace place, String flatNo);
    void deleteByPlaceAndFlatNo(NearbyPlace place, String flatNo);
}