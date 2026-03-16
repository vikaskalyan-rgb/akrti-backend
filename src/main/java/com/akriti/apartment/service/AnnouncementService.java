package com.akriti.apartment.service;

import com.akriti.apartment.dto.AnnouncementRequest;
import com.akriti.apartment.entity.Announcement;
import com.akriti.apartment.entity.Flat;
import com.akriti.apartment.repository.AnnouncementRepository;
import com.akriti.apartment.repository.FlatRepository;
import com.akriti.apartment.websocket.WebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private FlatRepository flatRepository;
    @Autowired private WhatsAppService whatsAppService;
    @Autowired private WebSocketPublisher wsPublisher;

    public List<Announcement> getAll() {
        return announcementRepository.findAllByOrderByIsPinnedDescPostedAtDesc();
    }

    // Residents see only announcements relevant to their role
    public List<Announcement> getForResident(String phone) {
        Flat flat = flatRepository.findByResidentPhone(phone)
            .orElse(flatRepository.findByOwnerPhone(phone).orElse(null));

        boolean isOwner = flat != null && !flat.getOwnerType().equals(Flat.OwnerType.RENTED);

        List<Announcement.Audience> allowed = isOwner
            ? List.of(Announcement.Audience.EVERYONE, Announcement.Audience.OWNERS, Announcement.Audience.RESIDENTS)
            : List.of(Announcement.Audience.EVERYONE, Announcement.Audience.RESIDENTS);

        return announcementRepository.findByAudienceInOrderByIsPinnedDescPostedAtDesc(allowed);
    }

    @Transactional
    public Announcement create(AnnouncementRequest req) {
        Announcement.AnnouncementType type = Announcement.AnnouncementType.valueOf(req.getType().toUpperCase());
        Announcement.Audience audience     = Announcement.Audience.valueOf(req.getAudience().toUpperCase());

        Announcement ann = Announcement.builder()
            .type(type)
            .audience(audience)
            .title(req.getTitle())
            .body(req.getBody())
            .postedBy("Admin")
            .postedAt(LocalDate.now())
            .isPinned(false)
            .build();

        Announcement saved = announcementRepository.save(ann);

        // Send WhatsApp notifications to target audience
        sendNotifications(saved);

        // Push WebSocket event
        wsPublisher.announcementPosted(saved.getId(), saved.getAudience().name());

        return saved;
    }

    @Transactional
    public Announcement togglePin(Long id) {
        Announcement ann = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Announcement not found"));
        ann.setIsPinned(!ann.getIsPinned());
        return announcementRepository.save(ann);
    }

    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }

    private void sendNotifications(Announcement ann) {
        List<Flat> flats = flatRepository.findByIsActiveTrue();

        List<String> phones = flats.stream()
            .filter(f -> f.getFloor() > 0)
            .flatMap(f -> {
                return switch (ann.getAudience()) {
                    case OWNERS   -> List.of(f.getOwnerPhone()).stream();
                    case RESIDENTS -> {
                        // physically living person: tenant if rented, owner otherwise
                        String phone = f.getOwnerType() == Flat.OwnerType.RENTED && f.getResidentPhone() != null
                            ? f.getResidentPhone() : f.getOwnerPhone();
                        yield List.of(phone).stream();
                    }
                    case EVERYONE -> {
                        List<String> ps = new java.util.ArrayList<>();
                        if (f.getOwnerPhone() != null) ps.add(f.getOwnerPhone());
                        if (f.getResidentPhone() != null && !f.getResidentPhone().equals(f.getOwnerPhone()))
                            ps.add(f.getResidentPhone());
                        yield ps.stream();
                    }
                };
            })
            .distinct()
            .filter(p -> p != null && !p.isBlank())
            .collect(Collectors.toList());

        phones.forEach(phone ->
            whatsAppService.sendAnnouncementNotification(phone, ann.getTitle(), ann.getBody())
        );
    }
}
