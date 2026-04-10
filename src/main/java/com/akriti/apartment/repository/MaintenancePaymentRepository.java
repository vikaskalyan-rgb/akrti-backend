package com.akriti.apartment.repository;

import com.akriti.apartment.entity.MaintenancePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenancePaymentRepository extends JpaRepository<MaintenancePayment, Long> {
    List<MaintenancePayment> findByMonthAndYear(int month, int year);
    List<MaintenancePayment> findByFlatNo(String flatNo);
    Optional<MaintenancePayment> findByFlatNoAndMonthAndYear(String flatNo, int month, int year);
    List<MaintenancePayment> findByFlatNoOrderByYearDescMonthDesc(String flatNo);
    boolean existsByFlatNoAndMonthAndYear(String flatNo, int month, int year);
    List<MaintenancePayment> findByFlatNoAndStatus(
            String flatNo, MaintenancePayment.PaymentStatus status);
    List<MaintenancePayment> findByFlatNoAndYearGreaterThanEqualOrderByYearAscMonthAsc(
            String flatNo, int year);

    // In MaintenancePaymentRepository.java
    @Query("SELECT m FROM MaintenancePayment m WHERE m.month = :month AND m.year = :year AND m.status IN ('UNPAID', 'PARTIAL')")
    List<MaintenancePayment> findUnpaidByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
