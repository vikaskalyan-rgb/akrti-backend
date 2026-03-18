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
}