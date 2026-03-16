package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlatRepository extends JpaRepository<Flat, String> {
    List<Flat> findByIsActiveTrue();
    List<Flat> findByWingAndIsActiveTrue(String wing);
    Optional<Flat> findByResidentPhone(String phone);
    Optional<Flat> findByOwnerPhone(String phone);
    List<Flat> findByOwnerPhoneOrResidentPhone(String ownerPhone, String residentPhone);
}
