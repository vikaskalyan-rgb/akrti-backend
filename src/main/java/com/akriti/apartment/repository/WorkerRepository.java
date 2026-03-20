package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findByIsActiveTrueOrderByRoleAscNameAsc();
    List<Worker> findByRoleAndIsActiveTrue(Worker.WorkerRole role);
}