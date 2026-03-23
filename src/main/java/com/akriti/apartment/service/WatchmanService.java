package com.akriti.apartment.service;

import com.akriti.apartment.entity.WatchmanLog;
import com.akriti.apartment.repository.WatchmanLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class WatchmanService {

    @Autowired
    private WatchmanLogRepository repo;

    // Night slots: 22,23,0,1,2,3,4,5
    public static final List<Integer> NIGHT_SLOTS = List.of(22,23,0,1,2,3,4,5);

    // For a given real date+time, get the "patrol date"
    // Night of 22 Apr starts at 22:00 on 22 Apr and ends at 06:00 on 23 Apr
    // Slots 0-5 belong to the PREVIOUS calendar date's patrol
    public static LocalDate patrolDateFor(LocalDateTime dt) {
        if (dt.getHour() < 6) return dt.toLocalDate().minusDays(1);
        return dt.toLocalDate();
    }

    // Log a whistle for the current hour
    public WatchmanLog logNow() {
        LocalDateTime now      = LocalDateTime.now();
        int           slot     = now.getHour();
        LocalDate     patrol   = patrolDateFor(now);

        if (!NIGHT_SLOTS.contains(slot)) {
            throw new RuntimeException("Whistle log is only for night hours (10PM – 6AM)");
        }
        if (repo.existsByLogDateAndHourSlot(patrol, slot)) {
            throw new RuntimeException("Already logged for this hour!");
        }
        return repo.save(WatchmanLog.builder()
                .logDate(patrol)
                .hourSlot(slot)
                .loggedAt(now)
                .loggedBy("SUP")
                .build());
    }

    // Get logs for a specific patrol date
    public List<WatchmanLog> getByDate(LocalDate date) {
        return repo.findByLogDateOrderByHourSlotAsc(date);
    }

    // Get last N patrol dates summary
    public List<Map<String, Object>> getSummary(int days) {
        LocalDate today   = LocalDate.now();
        // patrol dates go back from yesterday (today's night is ongoing)
        LocalDate from    = today.minusDays(days);
        LocalDate to      = today;

        List<WatchmanLog> logs = repo.findByLogDateBetweenOrderByLogDateDescHourSlotAsc(from, to);

        // Group by patrol date
        Map<LocalDate, List<WatchmanLog>> byDate = new LinkedHashMap<>();
        // Init all dates
        for (int i = 0; i < days; i++) {
            byDate.put(today.minusDays(i), new ArrayList<>());
        }
        logs.forEach(l -> byDate.computeIfAbsent(l.getLogDate(), k -> new ArrayList<>()).add(l));

        List<Map<String, Object>> result = new ArrayList<>();
        byDate.forEach((date, dateLogs) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("patrolDate",  date.toString());
            row.put("logged",      dateLogs.size());
            row.put("total",       NIGHT_SLOTS.size()); // 8
            row.put("logs",        dateLogs);
            row.put("missed",      NIGHT_SLOTS.size() - dateLogs.size());
            row.put("complete",    dateLogs.size() == NIGHT_SLOTS.size());
            result.add(row);
        });
        return result;
    }

    // Current patrol status (for watchman view)
    public Map<String, Object> getCurrentStatus() {
        LocalDateTime now    = LocalDateTime.now();
        int           slot   = now.getHour();
        LocalDate     patrol = patrolDateFor(now);
        boolean       isNight= NIGHT_SLOTS.contains(slot);

        List<WatchmanLog> logs = repo.findByLogDateOrderByHourSlotAsc(patrol);
        boolean alreadyLogged  = logs.stream().anyMatch(l -> l.getHourSlot() == slot);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("isNightTime",    isNight);
        res.put("currentSlot",    slot);
        res.put("patrolDate",     patrol.toString());
        res.put("alreadyLogged",  alreadyLogged);
        res.put("loggedSlots",    logs.stream().map(WatchmanLog::getHourSlot).toList());
        res.put("loggedCount",    logs.size());
        res.put("totalSlots",     NIGHT_SLOTS.size());
        return res;
    }
}