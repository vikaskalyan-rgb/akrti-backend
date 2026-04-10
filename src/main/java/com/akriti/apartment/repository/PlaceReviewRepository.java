package com.akriti.apartment.repository;

import com.akriti.apartment.entity.NearbyPlace;
import com.akriti.apartment.entity.PlaceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {
    List<PlaceReview> findByPlaceOrderByCreatedAtDesc(NearbyPlace place);
    int countByPlace(NearbyPlace place);
    void deleteByPlaceId(Long placeId);
}