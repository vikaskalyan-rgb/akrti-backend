package com.akriti.apartment.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class WebSocketPublisher {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Payment updated by resident → notify all subscribers
    public void paymentUpdated(String flatNo, int month, int year) {
        messagingTemplate.convertAndSend("/topic/payments", Map.of(
            "event", "PAYMENT_UPDATED",
            "flatNo", flatNo,
            "month", month,
            "year", year
        ));
    }

    // New complaint raised → notify admin
    public void complaintCreated(Long complaintId, String flatNo) {
        messagingTemplate.convertAndSend("/topic/complaints", Map.of(
            "event", "COMPLAINT_CREATED",
            "complaintId", complaintId,
            "flatNo", flatNo
        ));
    }

    // Complaint status changed → notify resident
    public void complaintUpdated(Long complaintId, String status) {
        messagingTemplate.convertAndSend("/topic/complaints", Map.of(
            "event", "COMPLAINT_UPDATED",
            "complaintId", complaintId,
            "status", status
        ));
    }

    // New announcement posted
    public void announcementPosted(Long announcementId, String audience) {
        messagingTemplate.convertAndSend("/topic/announcements", Map.of(
            "event", "ANNOUNCEMENT_POSTED",
            "announcementId", announcementId,
            "audience", audience
        ));
    }

    // New visitor logged
    public void visitorLogged(Long visitorId, String flatNo) {
        messagingTemplate.convertAndSend("/topic/visitors", Map.of(
            "event", "VISITOR_LOGGED",
            "visitorId", visitorId,
            "flatNo", flatNo
        ));
    }
}
