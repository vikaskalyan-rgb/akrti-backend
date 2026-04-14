package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByIsActiveTrueOrderByCreatedAtDesc();
}