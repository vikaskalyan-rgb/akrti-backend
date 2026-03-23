package com.akriti.apartment.repository;
import com.akriti.apartment.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findAllByOrderByLoggedAtDesc();
    List<Delivery> findByFlatNoOrderByLoggedAtDesc(String flatNo);
    List<Delivery> findByStatusOrderByLoggedAtDesc(String status);
}