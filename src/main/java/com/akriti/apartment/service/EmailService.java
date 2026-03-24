package com.akriti.apartment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${brevo.api.key:}")
    private String apiKey;

    public void sendMaintenanceReminder(String toEmail, String flatNo,
                                        String name, String month,
                                        int amount) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("📧 [SIMULATED REMINDER] To: {} | Flat: {} | Amount: {}",
                    toEmail, flatNo, amount);
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String html = """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px;">
              <div style="background:#e11d48;padding:20px;border-radius:12px 12px 0 0;text-align:center;">
                <h2 style="color:white;margin:0;font-size:20px;">Maintenance Due</h2>
                <p style="color:rgba(255,255,255,0.8);margin:4px 0 0;font-size:13px;">
                  Akriti Adeshwar Society
                </p>
              </div>
              <div style="background:#f8f8ff;padding:28px;border-radius:0 0 12px 12px;
                          border:1px solid #e0e0f0;">
                <p style="color:#333;font-size:14px;margin:0 0 12px;">
                  Dear <strong>%s</strong> (Flat <strong>%s</strong>),
                </p>
                <p style="color:#555;font-size:14px;margin:0 0 20px;">
                  Your maintenance payment for <strong>%s</strong> is pending.
                </p>
                <div style="background:white;border:2px solid #e11d48;border-radius:12px;
                            padding:20px;text-align:center;margin:0 0 20px;">
                  <div style="font-size:13px;color:#888;margin-bottom:4px;">Amount Due</div>
                  <span style="font-size:36px;font-weight:bold;color:#e11d48;">
                    ₹%s
                  </span>
                </div>
                <p style="color:#555;font-size:13px;margin:0 0 8px;">
                  Please pay via UPI and mark as paid on the society portal.
                </p>
                <p style="color:#888;font-size:11px;margin:0;">
                  Login at your society portal · Default password: flatNo@123
                </p>
              </div>
            </div>
            """.formatted(name, flatNo, month,
                    String.format("%,d", amount));

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of(
                    "name",  "Akriti Adeshwar Society",
                    "email", "akritiadeshwar.society@gmail.com"
            ));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject",
                    "Reminder: Maintenance Due for " + month + " — Flat " + flatNo);
            body.put("htmlContent", html);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email", request, String.class);
            log.info("✅ Reminder sent to flat {}", flatNo);

        } catch (Exception e) {
            log.error("Failed to send reminder to flat {}: {}", flatNo, e.getMessage());
        }
    }

    public void sendOtpEmail(String toEmail, String otp, String flatNo) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("📧 [SIMULATED EMAIL] To: {} | Flat: {} | OTP: {}", toEmail, flatNo, otp);
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String html = """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px;">
                  <div style="background:#5b52f0;padding:20px;border-radius:12px 12px 0 0;text-align:center;">
                    <h2 style="color:white;margin:0;font-size:20px;">Akriti Adeshwar</h2>
                    <p style="color:rgba(255,255,255,0.8);margin:4px 0 0;font-size:13px;">
                      Society Management Portal
                    </p>
                  </div>
                  <div style="background:#f8f8ff;padding:28px;border-radius:0 0 12px 12px;
                              border:1px solid #e0e0f0;">
                    <p style="color:#333;font-size:14px;margin:0 0 16px;">
                      Hi Flat <strong>%s</strong>,
                    </p>
                    <p style="color:#555;font-size:14px;margin:0 0 20px;">
                      Your password reset OTP is:
                    </p>
                    <div style="background:white;border:2px solid #5b52f0;border-radius:12px;
                                padding:20px;text-align:center;margin:0 0 20px;">
                      <span style="font-size:36px;font-weight:bold;color:#5b52f0;
                                   letter-spacing:8px;">%s</span>
                    </div>
                    <p style="color:#888;font-size:12px;margin:0;">
                      Valid for 10 minutes. Do not share this OTP with anyone.
                    </p>
                  </div>
                </div>
                """.formatted(flatNo, otp);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of(
                    "name",  "Akriti Adeshwar Society",
                    "email", "akritiadeshwar.society@gmail.com"
            ));
            body.put("to", List.of(Map.of(
                    "email", toEmail
            )));
            body.put("subject", "Akriti Adeshwar — Password Reset OTP");
            body.put("htmlContent", html);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email",
                    request,
                    String.class
            );

            log.info("✅ Brevo email sent: {}", response.getStatusCode());

        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    public void sendAnnouncementEmail(String toEmail, String name, String title,
                                      String body, String type, String audience) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("📧 [SIMULATED ANNOUNCEMENT] To: {} | {}", toEmail, title);
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String typeColor = switch (type.toUpperCase()) {
                case "URGENT" -> "#e11d48";
                case "EVENT"  -> "#059669";
                default       -> "#5b52f0";
            };

            String typeLabel = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();

            String html = """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px;">
              <div style="background:%s;padding:20px;border-radius:12px 12px 0 0;text-align:center;">
                <div style="display:inline-block;background:rgba(255,255,255,0.2);
                            padding:4px 12px;border-radius:20px;margin-bottom:8px;">
                  <span style="color:white;font-size:11px;font-weight:bold;
                               text-transform:uppercase;letter-spacing:1px;">%s</span>
                </div>
                <h2 style="color:white;margin:0;font-size:20px;">Akriti Adeshwar</h2>
                <p style="color:rgba(255,255,255,0.8);margin:4px 0 0;font-size:13px;">
                  Society Management Portal
                </p>
              </div>
              <div style="background:#f8f8ff;padding:28px;border-radius:0 0 12px 12px;
                          border:1px solid #e0e0f0;">
                <p style="color:#333;font-size:14px;margin:0 0 16px;">
                  Dear <strong>%s</strong>,
                </p>
                <h3 style="color:#1a1a2e;font-size:18px;margin:0 0 12px;
                           letter-spacing:-0.02em;">%s</h3>
                <p style="color:#555;font-size:14px;line-height:1.6;margin:0 0 20px;">%s</p>
                <div style="border-top:1px solid #e8e8f0;padding-top:16px;margin-top:4px;">
                  <p style="color:#888;font-size:11px;margin:0;">
                    Posted by Society Admin · Akriti Adeshwar
                  </p>
                </div>
              </div>
            </div>
            """.formatted(typeColor, typeLabel, name, title, body);

            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("sender", Map.of(
                    "name",  "Akriti Adeshwar Society",
                    "email", "akritiadeshwar.society@gmail.com"
            ));
            reqBody.put("to", List.of(Map.of("email", toEmail)));
            reqBody.put("subject", "[" + typeLabel + "] " + title + " — Akriti Adeshwar");
            reqBody.put("htmlContent", html);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(reqBody, headers);
            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email", request, String.class);
            log.info("✅ Announcement email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send announcement email: {}", e.getMessage());
            throw new RuntimeException("Failed to send announcement email");
        }
    }
    public void sendDeliveryNotification(String toEmail, String flatNo,
                                         String courierName, String description) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("📧 [SIMULATED DELIVERY] To: {} | Flat: {} | Courier: {}",
                    toEmail, flatNo, courierName);
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String html = """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px;">
              <div style="background:#5b52f0;padding:20px;border-radius:12px 12px 0 0;text-align:center;">
                <h2 style="color:white;margin:0;font-size:20px;">📦 Parcel Arrived!</h2>
                <p style="color:rgba(255,255,255,0.8);margin:4px 0 0;font-size:13px;">
                  Akriti Adeshwar Society
                </p>
              </div>
              <div style="background:#f8f8ff;padding:28px;border-radius:0 0 12px 12px;
                          border:1px solid #e0e0f0;">
                <p style="color:#333;font-size:14px;margin:0 0 12px;">
                  Dear Flat <strong>%s</strong>,
                </p>
                <p style="color:#555;font-size:14px;margin:0 0 20px;">
                  Your parcel has arrived at the security desk and is waiting for collection.
                </p>
                <div style="background:white;border:2px solid #5b52f0;border-radius:12px;
                            padding:20px;margin:0 0 20px;">
                  <div style="display:flex;justify-content:space-between;margin-bottom:8px;">
                    <span style="font-size:12px;color:#888;">Courier</span>
                    <span style="font-size:13px;font-weight:bold;color:#1a1a2e;">%s</span>
                  </div>
                  <div style="display:flex;justify-content:space-between;">
                    <span style="font-size:12px;color:#888;">Item</span>
                    <span style="font-size:13px;font-weight:bold;color:#1a1a2e;">%s</span>
                  </div>
                </div>
                <p style="color:#555;font-size:13px;margin:0 0 8px;">
                  Please collect it from the security desk at your earliest convenience.
                </p>
                <p style="color:#888;font-size:11px;margin:0;">
                  Akriti Adeshwar Society Management
                </p>
              </div>
            </div>
            """.formatted(
                    flatNo,
                    courierName != null ? courierName : "Unknown",
                    description != null ? description : "Parcel"
            );

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of(
                    "name",  "Akriti Adeshwar Society",
                    "email", "akritiadeshwar.society@gmail.com"
            ));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", "📦 Parcel arrived for Flat " + flatNo + " — Collect from Security");
            body.put("htmlContent", html);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email", request, String.class);
            log.info("✅ Delivery notification sent to flat {}", flatNo);

        } catch (Exception e) {
            log.error("Failed to send delivery notification to flat {}: {}", flatNo, e.getMessage());
        }
    }
    private static final List<String> ADMIN_EMAILS = List.of(
            "vikaskalyan1811@gmail.com",
            "sranjitht@gmail.com",
            "muraligk123@gmail.com",
            "kalyancbe30@gmail.com"
    );

    public void sendBookingRequestToAdmins(String flatNo, String bookedBy,
                                           String date, String startTime,
                                           String endTime, String purpose) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("📧 [SIMULATED] Booking request from Flat {} for {}", flatNo, date);
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String timeStr = (startTime != null ? startTime : "") +
                    (endTime   != null ? " – " + endTime : "");

            String html = """
            <div style="font-family:Arial,sans-serif;max-width:500px;margin:0 auto;padding:24px;">
              <div style="background:#5b52f0;padding:20px;border-radius:12px 12px 0 0;text-align:center;">
                <h2 style="color:white;margin:0;font-size:20px;">🏛 Hall Booking Request</h2>
                <p style="color:rgba(255,255,255,0.8);margin:4px 0 0;font-size:13px;">
                  Akriti Adeshwar Society
                </p>
              </div>
              <div style="background:#f8f8ff;padding:28px;border-radius:0 0 12px 12px;
                          border:1px solid #e0e0f0;">
                <p style="color:#333;font-size:14px;margin:0 0 16px;">
                  A new Community Hall booking request has been submitted.
                  Please review and approve or reject from the app.
                </p>
                <div style="background:white;border:1px solid #e0e0f0;border-radius:12px;
                            padding:20px;margin:0 0 20px;">
                  <table style="width:100%%;border-collapse:collapse;">
                    <tr>
                      <td style="padding:8px 0;font-size:12px;color:#888;width:120px;">Flat</td>
                      <td style="padding:8px 0;font-size:13px;font-weight:bold;color:#1a1a2e;">%s</td>
                    </tr>
                    <tr style="border-top:1px solid #f0f0f0;">
                      <td style="padding:8px 0;font-size:12px;color:#888;">Booked By</td>
                      <td style="padding:8px 0;font-size:13px;font-weight:bold;color:#1a1a2e;">%s</td>
                    </tr>
                    <tr style="border-top:1px solid #f0f0f0;">
                      <td style="padding:8px 0;font-size:12px;color:#888;">Date</td>
                      <td style="padding:8px 0;font-size:13px;font-weight:bold;color:#1a1a2e;">%s</td>
                    </tr>
                    <tr style="border-top:1px solid #f0f0f0;">
                      <td style="padding:8px 0;font-size:12px;color:#888;">Time</td>
                      <td style="padding:8px 0;font-size:13px;font-weight:bold;color:#1a1a2e;">%s</td>
                    </tr>
                    <tr style="border-top:1px solid #f0f0f0;">
                      <td style="padding:8px 0;font-size:12px;color:#888;">Purpose</td>
                      <td style="padding:8px 0;font-size:13px;font-weight:bold;color:#1a1a2e;">%s</td>
                    </tr>
                  </table>
                </div>
                <div style="text-align:center;margin:0 0 16px;">
                  <a href="https://akrti-backend.onrender.com"
                     style="display:inline-block;background:#5b52f0;color:white;
                            padding:14px 32px;border-radius:10px;text-decoration:none;
                            font-size:14px;font-weight:bold;">
                    Open App to Approve / Reject
                  </a>
                </div>
                <p style="color:#888;font-size:11px;text-align:center;margin:0;">
                  Login → Hall Booking → Pending requests
                </p>
              </div>
            </div>
            """.formatted(flatNo, bookedBy != null ? bookedBy : flatNo,
                    date, timeStr, purpose);

            // Send to all 4 admins
            List<Map<String, String>> toList = ADMIN_EMAILS.stream()
                    .map(email -> Map.of("email", email))
                    .toList();

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of(
                    "name",  "Akriti Adeshwar Society",
                    "email", "akritiadeshwar.society@gmail.com"
            ));
            body.put("to", toList);
            body.put("subject", "🏛 Hall Booking Request — Flat " + flatNo + " · " + date);
            body.put("htmlContent", html);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email", request, String.class);
            log.info("✅ Booking request email sent to {} admins", ADMIN_EMAILS.size());

        } catch (Exception e) {
            log.error("Failed to send booking request email: {}", e.getMessage());
        }
    }

    public void sendBookingStatusToResident(String toEmail, String flatNo,
                                            String purpose, String date,
                                            String status, String adminNote) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("📧 [SIMULATED] Booking {} for flat {}", status, flatNo);
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            boolean approved  = "APPROVED".equals(status);
            String  color     = approved ? "#059669" : "#e11d48";
            String  emoji     = approved ? "✅" : "❌";
            String  statusTxt = approved ? "Approved" : "Rejected";

            String noteHtml = (adminNote != null && !adminNote.isBlank())
                    ? """
              <div style="background:#f0f0f8;border-radius:8px;padding:12px;margin-top:12px;">
                <p style="margin:0;font-size:12px;color:#555;">
                  <strong>Admin Note:</strong> %s
                </p>
              </div>
              """.formatted(adminNote)
                    : "";

            String html = """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px;">
              <div style="background:%s;padding:20px;border-radius:12px 12px 0 0;text-align:center;">
                <h2 style="color:white;margin:0;font-size:20px;">%s Hall Booking %s</h2>
                <p style="color:rgba(255,255,255,0.8);margin:4px 0 0;font-size:13px;">
                  Akriti Adeshwar Society
                </p>
              </div>
              <div style="background:#f8f8ff;padding:28px;border-radius:0 0 12px 12px;
                          border:1px solid #e0e0f0;">
                <p style="color:#333;font-size:14px;margin:0 0 16px;">
                  Dear Flat <strong>%s</strong>,
                </p>
                <p style="color:#555;font-size:14px;margin:0 0 16px;">
                  Your booking request for <strong>%s</strong> on <strong>%s</strong>
                  has been <strong style="color:%s;">%s</strong>.
                </p>
                %s
                <p style="color:#888;font-size:11px;margin-top:16px;">
                  View details on the society portal under Hall Booking.
                </p>
              </div>
            </div>
            """.formatted(color, emoji, statusTxt,
                    flatNo, purpose, date, color, statusTxt, noteHtml);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of(
                    "name",  "Akriti Adeshwar Society",
                    "email", "akritiadeshwar.society@gmail.com"
            ));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", emoji + " Hall Booking " + statusTxt + " — " + purpose);
            body.put("htmlContent", html);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email", request, String.class);
            log.info("✅ Booking {} email sent to flat {}", status, flatNo);

        } catch (Exception e) {
            log.error("Failed to send booking status email: {}", e.getMessage());
        }
    }
}