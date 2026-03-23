package com.akriti.apartment.repository;
import com.akriti.apartment.entity.AmenityBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AmenityBookingRepository extends JpaRepository<AmenityBooking, Long> {
    List<AmenityBooking> findAllByOrderByBookingDateDescCreatedAtDesc();
    List<AmenityBooking> findByFlatNoOrderByBookingDateDesc(String flatNo);
    List<AmenityBooking> findByBookingDateOrderByStartTimeAsc(LocalDate date);
    boolean existsByAmenityAndBookingDateAndStatusNot(
            String amenity, LocalDate date, String status);
}