package com.akriti.apartment.scheduler;

import com.akriti.apartment.repository.FlatRepository;
import com.akriti.apartment.repository.StepLogRepository;
import com.akriti.apartment.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StepReportScheduler {

    @Autowired
    private StepLogRepository logRepo;

    @Autowired
    private FlatRepository flatRepo;

    @Autowired
    private EmailService emailService;

    // Runs 1st of every month at 8:00 AM IST
    @Scheduled(cron = "0 0 8 1 * *", zone = "Asia/Kolkata")
    public void sendMonthlyStepReport() {
        LocalDate today    = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate firstDay = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDay  = today.minusDays(1);

        List<Object[]> leaderboard = logRepo.monthlyLeaderboard(firstDay, lastDay);
        if (leaderboard.isEmpty()) return;

        String monthLabel = firstDay.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        // Build HTML table rows
        StringBuilder rows = new StringBuilder();
        int rank = 1;
        for (Object[] row : leaderboard) {
            String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : rank + ".";
            String bg    = rank == 1 ? "#fffbeb" : rank == 2 ? "#f9fafb" : rank == 3 ? "#fef3c7" : "white";
            long   steps = ((Number) row[2]).longValue();
            rows.append(String.format("""
                <tr style="background:%s;border-bottom:1px solid #e5e7eb">
                  <td style="padding:12px 16px;font-size:18px">%s</td>
                  <td style="padding:12px 16px;font-weight:600;color:#1a1a2e">%s</td>
                  <td style="padding:12px 16px;color:#6b7280">Flat %s</td>
                  <td style="padding:12px 16px;font-weight:700;color:#5b52f0">%,d steps</td>
                </tr>
                """, bg, medal, row[1], row[0], steps));
            rank++;
        }

        String subject = "🏃 Step Challenge Results — " + monthLabel + " | Akriti Adeshwar";
        String html = String.format("""
            <div style="font-family:sans-serif;max-width:600px;margin:0 auto;padding:16px">
              <div style="background:linear-gradient(135deg,#5b52f0,#059669);padding:32px;
                          border-radius:16px 16px 0 0;text-align:center">
                <div style="font-size:56px">🏃</div>
                <h1 style="color:white;margin:8px 0 4px;font-size:26px;letter-spacing:-0.02em">
                  Monthly Step Challenge
                </h1>
                <p style="color:#c7c4fc;margin:0;font-size:14px">
                  %s · Akriti Adeshwar Apartments
                </p>
              </div>

              <div style="background:white;padding:28px;border:1px solid #e5e7eb;
                          border-radius:0 0 16px 16px">
                <h2 style="color:#1a1a2e;margin:0 0 16px;font-size:18px">
                  🏆 Final Leaderboard
                </h2>
                <table style="width:100%%;border-collapse:collapse;
                              border:1px solid #e5e7eb;border-radius:12px;overflow:hidden">
                  <thead>
                    <tr style="background:#5b52f0">
                      <th style="padding:10px 16px;color:white;text-align:left;font-size:12px">Rank</th>
                      <th style="padding:10px 16px;color:white;text-align:left;font-size:12px">Name</th>
                      <th style="padding:10px 16px;color:white;text-align:left;font-size:12px">Flat</th>
                      <th style="padding:10px 16px;color:white;text-align:left;font-size:12px">Steps</th>
                    </tr>
                  </thead>
                  <tbody>%s</tbody>
                </table>

                <div style="margin-top:20px;padding:16px;background:#ecfdf5;
                            border-radius:12px;border:1px solid #6ee7b7">
                  <p style="margin:0;color:#065f46;font-size:13px;line-height:1.5">
                    🎉 <strong>Congratulations to our top walkers!</strong><br/>
                    Top 5 winners will receive prizes at the Annual Day celebration.
                    Keep walking and stay healthy! 💪
                  </p>
                </div>

                <p style="color:#9ca3af;font-size:11px;text-align:center;margin-top:20px">
                  Akriti Adeshwar Society Management App · Auto-generated monthly report
                </p>
              </div>
            </div>
            """, monthLabel, rows.toString());

        // Send to all residents with registered email
        flatRepo.findAll().forEach(flat -> {
            try {
                if (flat.getOwnerEmail() != null && !flat.getOwnerEmail().isBlank()) {
                    emailService.sendHtmlEmail(flat.getOwnerEmail(), subject, html);
                }
                // residentEmail is the tenant's email when flat is RENTED
                if (flat.getResidentEmail() != null && !flat.getResidentEmail().isBlank()) {
                    emailService.sendHtmlEmail(flat.getResidentEmail(), subject, html);
                }
            } catch (Exception e) {
                System.err.println("Failed to send step report to flat "
                        + flat.getFlatNo() + ": " + e.getMessage());
            }
        });
    }
}