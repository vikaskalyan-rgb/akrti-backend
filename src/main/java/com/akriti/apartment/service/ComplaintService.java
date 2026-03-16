package com.akriti.apartment.service;

import com.akriti.apartment.dto.ComplaintRequest;
import com.akriti.apartment.dto.UpdateComplaintRequest;
import com.akriti.apartment.entity.Complaint;
import com.akriti.apartment.entity.Flat;
import com.akriti.apartment.repository.ComplaintRepository;
import com.akriti.apartment.repository.FlatRepository;
import com.akriti.apartment.websocket.WebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ComplaintService {

    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private FlatRepository flatRepository;
    @Autowired private WebSocketPublisher wsPublisher;

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Complaint> getComplaintsByFlat(String flatNo) {
        return complaintRepository.findByFlatNoOrderByCreatedAtDesc(flatNo);
    }

    public Complaint getComplaint(Long id) {
        return complaintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Complaint not found"));
    }

    @Transactional
    public Complaint createComplaint(ComplaintRequest req, String callerPhone) {
        // Resolve flat and resident name from phone
        Flat flat = flatRepository.findByResidentPhone(callerPhone)
            .orElse(flatRepository.findByOwnerPhone(callerPhone)
                .orElseThrow(() -> new RuntimeException("Flat not found for this user")));

        Complaint complaint = Complaint.builder()
            .flatNo(flat.getFlatNo())
            .residentName(flat.getOwnerType() == Flat.OwnerType.RENTED && callerPhone.equals(flat.getResidentPhone())
                ? flat.getResidentName() : flat.getOwnerName())
            .category(Complaint.Category.valueOf(req.getCategory().toUpperCase()))
            .title(req.getTitle())
            .description(req.getDescription())
            .priority(Complaint.Priority.valueOf(req.getPriority().toUpperCase()))
            .status(Complaint.Status.OPEN)
            .createdAt(LocalDate.now())
            .updatedAt(LocalDate.now())
            .build();

        Complaint saved = complaintRepository.save(complaint);

        // Notify admin via WebSocket
        wsPublisher.complaintCreated(saved.getId(), saved.getFlatNo());

        return saved;
    }

    @Transactional
    public Complaint updateStatus(Long id, UpdateComplaintRequest req) {
        Complaint complaint = complaintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Complaint not found"));

        Complaint.Status newStatus = Complaint.Status.valueOf(
            req.getStatus().toUpperCase().replace("-", "_")
        );
        complaint.setStatus(newStatus);
        complaint.setUpdatedAt(LocalDate.now());

        Complaint saved = complaintRepository.save(complaint);

        // Notify resident via WebSocket
        wsPublisher.complaintUpdated(saved.getId(), saved.getStatus().name());

        return saved;
    }

    public Map<String, Long> getComplaintCounts() {
        return Map.of(
            "open",       complaintRepository.countByStatus(Complaint.Status.OPEN),
            "inProgress", complaintRepository.countByStatus(Complaint.Status.IN_PROGRESS),
            "resolved",   complaintRepository.countByStatus(Complaint.Status.RESOLVED)
        );
    }
}
