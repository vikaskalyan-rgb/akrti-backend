package com.akriti.apartment.service;

import com.akriti.apartment.dto.FlatRequest;
import com.akriti.apartment.entity.Flat;
import com.akriti.apartment.entity.MaintenancePayment;
import com.akriti.apartment.entity.User;
import com.akriti.apartment.repository.FlatRepository;
import com.akriti.apartment.repository.MaintenancePaymentRepository;
import com.akriti.apartment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class FlatService {

    private static final Logger log = LoggerFactory.getLogger(FlatService.class);

    @Autowired private FlatRepository flatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MaintenancePaymentRepository paymentRepository;

    public List<Flat> getAll() {
        return flatRepository.findByIsActiveTrue();
    }

    public Flat getFlat(String flatNo) {
        return flatRepository.findById(flatNo)
                .orElseThrow(() -> new RuntimeException("Flat not found: " + flatNo));
    }

    @Transactional
    public Flat update(String flatNo, FlatRequest req) {
        Flat flat = getFlat(flatNo);

        if (req.getOwnerName()     != null) flat.setOwnerName(req.getOwnerName());
        if (req.getOwnerPhone()    != null) flat.setOwnerPhone(req.getOwnerPhone());
        if (req.getOwnerEmail()    != null) flat.setOwnerEmail(req.getOwnerEmail());
        if (req.getResidentName()  != null) flat.setResidentName(req.getResidentName());
        if (req.getResidentPhone() != null) flat.setResidentPhone(req.getResidentPhone());
        if (req.getResidentEmail() != null) flat.setResidentEmail(req.getResidentEmail());
        if (req.getParkingSlot()   != null) flat.setParkingSlot(req.getParkingSlot());
        if (req.getMaintenanceAmount() != null) flat.setMaintenanceAmount(req.getMaintenanceAmount());
        if (req.getOwnerType()     != null) {
            flat.setOwnerType(Flat.OwnerType.valueOf(
                    req.getOwnerType().toUpperCase()
                            .replace("-", "_").replace(" ", "_")));
        }

        Flat saved = flatRepository.save(flat);
        // Sync maintenance amount to ALL UNPAID payment records for this flat
        if (req.getMaintenanceAmount() != null) {
            List<MaintenancePayment> unpaidPayments = paymentRepository
                    .findByFlatNoAndStatus(flat.getFlatNo(),
                            MaintenancePayment.PaymentStatus.UNPAID);

            if (!unpaidPayments.isEmpty()) {
                unpaidPayments.forEach(p -> p.setAmount(req.getMaintenanceAmount()));
                paymentRepository.saveAll(unpaidPayments);
                log.info("✅ Updated {} unpaid dues for {} to ₹{}",
                        unpaidPayments.size(), flat.getFlatNo(), req.getMaintenanceAmount());
            }
        }

        // Sync to users table
        syncOwnerUser(saved);
        syncTenantUser(saved);

        return saved;
    }

    // ── Sync owner/admin user ──────────────────────────────────
    private void syncOwnerUser(Flat flat) {
        if (flat.getOwnerName() == null
                || flat.getOwnerName().equals("Unknown")
                || flat.getOwnerName().isBlank()) return;

        String identifier = flat.getFlatNo();
        String defaultPw  = flat.getFlatNo() + "@123";

        User.Role role = User.Role.OWNER;
        if (List.of("2H","4B","4J","2J").contains(flat.getFlatNo()))
            role = User.Role.ADMIN;

        Optional<User> existing = userRepository.findByIdentifier(identifier);

        if (existing.isEmpty()) {
            // Create new owner user
            User user = User.builder()
                    .flatNo(flat.getFlatNo())
                    .identifier(identifier)
                    .name(flat.getOwnerName())
                    .role(role)
                    .phone(flat.getOwnerPhone())
                    .email(flat.getOwnerEmail())
                    .passwordHash(passwordEncoder.encode(defaultPw))
                    .firstLogin(true)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            log.info("✅ Created owner user: {} with default password", identifier);
        } else {
            // Update existing — always sync name, phone, email
            User user = existing.get();
            user.setName(flat.getOwnerName());
            if (flat.getOwnerPhone() != null) user.setPhone(flat.getOwnerPhone());
            if (flat.getOwnerEmail() != null) user.setEmail(flat.getOwnerEmail());
            userRepository.save(user);
            log.info("✅ Synced owner user: {}", identifier);
        }
    }

    // ── Sync tenant user ───────────────────────────────────────
    private void syncTenantUser(Flat flat) {
        String identifier = flat.getFlatNo() + "_tenant";

        if (flat.getOwnerType() == Flat.OwnerType.RENTED
                && flat.getResidentName() != null
                && !flat.getResidentName().isBlank()) {

            String defaultPw = flat.getFlatNo() + "_tenant@123";
            Optional<User> existing = userRepository.findByIdentifier(identifier);

            if (existing.isEmpty()) {
                // Create new tenant user
                User user = User.builder()
                        .flatNo(flat.getFlatNo())
                        .identifier(identifier)
                        .name(flat.getResidentName())
                        .role(User.Role.TENANT)
                        .phone(flat.getResidentPhone())
                        .email(flat.getResidentEmail())
                        .passwordHash(passwordEncoder.encode(defaultPw))
                        .firstLogin(true)
                        .isActive(true)
                        .build();
                userRepository.save(user);
                log.info("✅ Created tenant user: {} with default password", identifier);
            } else {
                // Update existing tenant
                User user = existing.get();
                user.setName(flat.getResidentName());
                if (flat.getResidentPhone() != null) user.setPhone(flat.getResidentPhone());
                if (flat.getResidentEmail() != null) user.setEmail(flat.getResidentEmail());
                userRepository.save(user);
                log.info("✅ Synced tenant user: {}", identifier);
            }

        } else {
            // Flat is no longer rented — deactivate tenant user if exists
            userRepository.findByIdentifier(identifier).ifPresent(u -> {
                u.setIsActive(false);
                userRepository.save(u);
                log.info("⚠️ Deactivated tenant user: {} (flat no longer rented)", identifier);
            });
        }
    }
}