package com.akriti.apartment.service;

import com.akriti.apartment.dto.AnnouncementRequest;
import com.akriti.apartment.entity.Announcement;
import com.akriti.apartment.entity.Flat;
import com.akriti.apartment.entity.User;
import com.akriti.apartment.repository.AnnouncementRepository;
import com.akriti.apartment.repository.FlatRepository;
import com.akriti.apartment.repository.UserRepository;
import com.akriti.apartment.websocket.WebSocketPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private FlatRepository flatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private WebSocketPublisher wsPublisher;

    public List<Announcement> getAll() {
        return announcementRepository.findAllByOrderByIsPinnedDescPostedAtDesc();
    }

    public List<Announcement> getForResident(String identifier) {
        // Find user by identifier
        User user = userRepository.findByIdentifier(identifier).orElse(null);
        boolean isOwner = user != null && user.getRole() != User.Role.TENANT;

        List<Announcement.Audience> allowed = isOwner
                ? List.of(Announcement.Audience.EVERYONE, Announcement.Audience.OWNERS, Announcement.Audience.RESIDENTS)
                : List.of(Announcement.Audience.EVERYONE, Announcement.Audience.RESIDENTS);

        return announcementRepository.findByAudienceInOrderByIsPinnedDescPostedAtDesc(allowed);
    }

    // ── Get real recipient count from users table ─────────
    public int getRecipientCount(String audience) {
        Announcement.Audience aud = Announcement.Audience.valueOf(audience.toUpperCase());
        return getRecipients(aud).size();
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

        // Send email notifications
        sendEmailNotifications(saved);

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

    // ── Get recipients from users table ───────────────────
    private List<User> getRecipients(Announcement.Audience audience) {
        List<User> allUsers = userRepository.findAll().stream()
                .filter(u -> u.getIsActive() != null && u.getIsActive())
                .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                .collect(Collectors.toList());

        return switch (audience) {
            case EVERYONE ->
                // All active users with email — owners + tenants, no duplicates
                    allUsers.stream()
                            .filter(u -> u.getRole() != null)
                            .collect(Collectors.toList());

            case OWNERS ->
                // Only owners and admins (not tenants)
                    allUsers.stream()
                            .filter(u -> u.getRole() == User.Role.OWNER || u.getRole() == User.Role.ADMIN)
                            .collect(Collectors.toList());

            case RESIDENTS ->
                // Physically living: owner-occupied owners + tenants
                    allUsers.stream()
                            .filter(u -> {
                                if (u.getRole() == User.Role.TENANT) return true;
                                if (u.getRole() == User.Role.OWNER || u.getRole() == User.Role.ADMIN) {
                                    // Include owner only if flat is owner-occupied (not rented)
                                    if (u.getFlatNo() == null) return false;
                                    Flat flat = flatRepository.findById(u.getFlatNo()).orElse(null);
                                    return flat != null && flat.getOwnerType() != Flat.OwnerType.RENTED;
                                }
                                return false;
                            })
                            .collect(Collectors.toList());
        };
    }

    // ── Send announcement emails ───────────────────────────
    private void sendEmailNotifications(Announcement ann) {
        List<User> recipients = getRecipients(ann.getAudience());
        int sent = 0, skipped = 0;

        for (User user : recipients) {
            try {
                emailService.sendAnnouncementEmail(
                        user.getEmail(),
                        user.getName(),
                        ann.getTitle(),
                        ann.getBody(),
                        ann.getType().name(),
                        ann.getAudience().name()
                );
                sent++;
            } catch (Exception e) {
                log.error("Failed to send announcement email to {}: {}", user.getEmail(), e.getMessage());
                skipped++;
            }
        }
        log.info("📧 Announcement emails: {} sent, {} skipped", sent, skipped);
    }
}