package com.akriti.apartment.repository;

import com.akriti.apartment.entity.WeeklyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WeeklyActivityRepository extends JpaRepository<WeeklyActivity, Long> {
    List<WeeklyActivity> findAllByOrderByDateAsc();
}