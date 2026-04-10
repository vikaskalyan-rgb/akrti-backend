package com.akriti.apartment.repository;

import com.akriti.apartment.entity.StepWalker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StepWalkerRepository extends JpaRepository<StepWalker, Long> {

    List<StepWalker> findByFlatNoOrderByWalkerNameAsc(String flatNo);

    Optional<StepWalker> findByFlatNoAndWalkerName(String flatNo, String walkerName);

    boolean existsByFlatNoAndWalkerName(String flatNo, String walkerName);
}