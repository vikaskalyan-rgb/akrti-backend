package com.akriti.apartment.scheduler;

import com.akriti.apartment.service.EmailService;
import com.akriti.apartment.service.MaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Map;

@Component
public class MaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceScheduler.class);

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired private EmailService emailService;

    // Runs at 00:01 on the 1st of every month
    @Scheduled(cron = "0 1 0 1 * *")
    public void generateMonthlyDues() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year  = today.getYear();

        log.info("⏰ Scheduler: Generating maintenance dues for {}/{}", month, year);

        try {
            int count = maintenanceService.generateMonthlyDues(month, year);
            log.info("✅ Generated {} payment records for {}/{}", count, month, year);
        } catch (Exception e) {
            log.error("❌ Failed to generate dues for {}/{}: {}", month, year, e.getMessage());
        }
    }

    // Send reminders to defaulters on 10th of every month at 10 AM
    @Scheduled(cron = "0 0 10 10 * *")
    public void sendDefaulterReminders() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year  = today.getYear();

        log.info("📧 Scheduler: Sending email reminders for {}/{}", month, year);

        try {
            Map<String, Object> result = maintenanceService.sendReminders(month, year);
            log.info("✅ {}", result.get("message"));
        } catch (Exception e) {
            log.error("❌ Failed to send reminders: {}", e.getMessage());
        }
    }

    // Send monthly dues generated notification to ALL residents on 1st at 9 AM
    @Scheduled(cron = "0 0 9 1 * *")
    public void sendMonthlyDuesGeneratedNotification() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year  = today.getYear();

        String monthLabel = today.format(
                java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy",
                        java.util.Locale.ENGLISH)
        );

        log.info("📧 Scheduler: Sending monthly dues notification for {}/{}", month, year);

        try {
            // First generate dues
            maintenanceService.generateMonthlyDues(month, year);
            log.info("✅ Dues generated for {}/{}", month, year);

            // Then notify all residents
            emailService.sendMonthlyDuesNotification(month, year);
            log.info("✅ Monthly dues notification sent for {}/{}", month, year);

        } catch (Exception e) {
            log.error("❌ Failed monthly dues notification: {}", e.getMessage());
        }
    }
}