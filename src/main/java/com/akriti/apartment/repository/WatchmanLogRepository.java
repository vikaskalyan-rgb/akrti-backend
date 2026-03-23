package com.akriti.apartment.repository;

import com.akriti.apartment.entity.WatchmanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface WatchmanLogRepository extends JpaRepository<WatchmanLog, Long> {
    List<WatchmanLog> findByLogDateOrderByHourSlotAsc(LocalDate date);
    List<WatchmanLog> findByLogDateBetweenOrderByLogDateDescHourSlotAsc(
            LocalDate from, LocalDate to);
    boolean existsByLogDateAndHourSlot(LocalDate date, int hourSlot);
}