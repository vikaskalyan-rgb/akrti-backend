package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    List<Visitor> findByInTimeBetweenOrderByInTimeDesc(LocalDateTime start, LocalDateTime end);
    List<Visitor> findAllByOrderByInTimeDesc();
    List<Visitor> findByFlatNoOrderByInTimeDesc(String flatNo);
    List<Visitor> findByStatusOrderByInTimeDesc(Visitor.VisitorStatus status);
    long countByStatus(Visitor.VisitorStatus status);
}
