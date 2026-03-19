package com.akriti.apartment.repository;

import com.akriti.apartment.entity.MaintenancePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT p FROM MaintenancePayment p WHERE p.month = :month AND p.year = :year AND p.status = 'UNPAID'")
    List<MaintenancePayment> findUnpaidByMonthAndYear(int month, int year);
}
