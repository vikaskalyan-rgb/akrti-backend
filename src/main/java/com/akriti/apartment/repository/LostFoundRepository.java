package com.akriti.apartment.repository;
import com.akriti.apartment.entity.LostFound;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LostFoundRepository extends JpaRepository<LostFound, Long> {
    List<LostFound> findAllByOrderByCreatedAtDesc();
}