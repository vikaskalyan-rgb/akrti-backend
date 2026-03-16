package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByFlatNoOrderByCreatedAtDesc(String flatNo);
    List<Complaint> findAllByOrderByCreatedAtDesc();
    List<Complaint> findByStatusOrderByCreatedAtDesc(Complaint.Status status);
    long countByStatus(Complaint.Status status);
}
