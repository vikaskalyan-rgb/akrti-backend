package com.akriti.apartment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    @Value("${fast2sms.api.key:}")
    private String apiKey;

    @Value("${app.otp.simulate:true}")
    private boolean simulate;

    public void sendOtp(String phone, String otp) {
        if (simulate || apiKey == null || apiKey.isBlank()) {
            log.info("📱 [SIMULATED SMS] To: {} | OTP: {}", phone, otp);
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("route", "q");
            body.put("message", "Your Akriti Adeshwar login OTP is: " + otp + ". Valid for 10 minutes. Do not share with anyone.");
            body.put("numbers", phone);
            body.put("flash", 0);
            body.put("language", "english");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://www.fast2sms.com/dev/bulkV2",
                    request,
                    String.class
            );

            log.info("Fast2SMS response: {}", response.getBody());

        } catch (Exception e) {
            log.error("Failed to send SMS OTP: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP. Please try again.");
        }
    }
}