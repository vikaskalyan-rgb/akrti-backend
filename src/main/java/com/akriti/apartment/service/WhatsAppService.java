package com.akriti.apartment.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String fromNumber;

    @Value("${app.otp.simulate:true}")
    private boolean simulate;

    @PostConstruct
    public void init() {
        if (!simulate) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized");
        } else {
            log.info("WhatsApp running in SIMULATE mode — messages logged only");
        }
    }

    public void sendOtp(String toPhone, String otp) {
        String message = String.format(
            "🏠 *Akriti Adeshwar*\n\nYour login OTP is: *%s*\n\nValid for 10 minutes. Do not share this with anyone.",
            otp
        );
        send(toPhone, message);
    }

    public void sendMaintenanceReminder(String toPhone, String residentName, String flatNo, int amount, String monthLabel) {
        String message = String.format(
            "🏠 *Akriti Adeshwar Society*\n\nDear %s,\n\nYour maintenance of ₹%d for *%s* (Flat %s) is due.\n\nPlease log in to the portal to mark your payment.\n\nThank you.",
            residentName, amount, monthLabel, flatNo
        );
        send(toPhone, message);
    }

    public void sendAnnouncementNotification(String toPhone, String title, String body) {
        String message = String.format(
            "📢 *Akriti Adeshwar — Announcement*\n\n*%s*\n\n%s",
            title, body
        );
        send(toPhone, message);
    }

    private void send(String toPhone, String message) {
        if (simulate) {
            log.info("📱 [SIMULATED WhatsApp] To: +91{} | Message: {}", toPhone, message);
            return;
        }
        try {
            Message.creator(
                new PhoneNumber("whatsapp:+91" + toPhone),
                new PhoneNumber(fromNumber),
                message
            ).create();
            log.info("WhatsApp sent to +91{}", toPhone);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp to +91{}: {}", toPhone, e.getMessage());
        }
    }
}
