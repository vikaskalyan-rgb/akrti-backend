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
}