package com.akriti.apartment.repository;

import com.akriti.apartment.entity.CommunityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityEventRepository extends JpaRepository<CommunityEvent, Long> {
    List<CommunityEvent> findAllByOrderByCreatedAtDesc();
}