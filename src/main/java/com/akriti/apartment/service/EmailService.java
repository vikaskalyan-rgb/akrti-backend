package com.akriti.apartment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, String flatNo) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.info("📧 [SIMULATED EMAIL] To: {} | Flat: {} | OTP: {}", toEmail, flatNo, otp);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();;
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Akriti Adeshwar Society");
            helper.setTo(toEmail);
            helper.setSubject("Akriti Adeshwar — Password Reset OTP");

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

            helper.setText(html, true);
            mailSender.send(message);
            log.info("✅ OTP email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }
}