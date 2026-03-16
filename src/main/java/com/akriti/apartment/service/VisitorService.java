package com.akriti.apartment.service;

import com.akriti.apartment.dto.VisitorRequest;
import com.akriti.apartment.entity.Visitor;
import com.akriti.apartment.repository.VisitorRepository;
import com.akriti.apartment.websocket.WebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class VisitorService {

    @Autowired private VisitorRepository visitorRepository;
    @Autowired private WebSocketPublisher wsPublisher;

    public List<Visitor> getAll() {
        return visitorRepository.findAllByOrderByInTimeDesc();
    }

    public List<Visitor> getToday() {
        LocalDate today = LocalDate.now();
        return visitorRepository.findByInTimeBetweenOrderByInTimeDesc(
            today.atStartOfDay(), today.plusDays(1).atStartOfDay()
        );
    }

    public List<Visitor> getByFlat(String flatNo) {
        return visitorRepository.findByFlatNoOrderByInTimeDesc(flatNo);
    }

    @Transactional
    public Visitor logEntry(VisitorRequest req) {
        Visitor visitor = Visitor.builder()
            .name(req.getName())
            .purpose(req.getPurpose())
            .flatNo(req.getFlatNo())
            .residentName(req.getResidentName())
            .phone(req.getPhone())
            .vehicleNo(req.getVehicleNo())
            .inTime(LocalDateTime.now())
            .status(Visitor.VisitorStatus.IN)
            .build();

        Visitor saved = visitorRepository.save(visitor);
        wsPublisher.visitorLogged(saved.getId(), saved.getFlatNo());
        return saved;
    }

    @Transactional
    public Visitor checkout(Long id) {
        Visitor visitor = visitorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Visitor not found"));
        visitor.setOutTime(LocalDateTime.now());
        visitor.setStatus(Visitor.VisitorStatus.OUT);
        return visitorRepository.save(visitor);
    }

    public Map<String, Long> getStats() {
        return Map.of(
            "currentlyInside", visitorRepository.countByStatus(Visitor.VisitorStatus.IN),
            "todayTotal", (long) getToday().size(),
            "todayExited", getToday().stream()
                .filter(v -> v.getStatus() == Visitor.VisitorStatus.OUT).count()
        );
    }
}
