package com.akriti.apartment.controller;

import com.akriti.apartment.entity.StepLog;
import com.akriti.apartment.entity.StepWalker;
import com.akriti.apartment.repository.StepLogRepository;
import com.akriti.apartment.repository.StepWalkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/steps")
public class StepsController {

    @Autowired
    private StepWalkerRepository walkerRepo;

    @Autowired
    private StepLogRepository logRepo;

    private LocalDate today() {
        return LocalDate.now(ZoneId.of("Asia/Kolkata"));
    }

    // ── Get all walkers for a flat ────────────────────────
    @GetMapping("/walkers/{flatNo}")
    public ResponseEntity<?> getWalkers(@PathVariable String flatNo) {
        return ResponseEntity.ok(walkerRepo.findByFlatNoOrderByWalkerNameAsc(flatNo));
    }

    // ── Add a walker to a flat ────────────────────────────
    @PostMapping("/walkers/{flatNo}")
    public ResponseEntity<?> addWalker(
            @PathVariable String flatNo,
            @RequestBody Map<String, String> body) {
        String name = body.get("walkerName");
        if (name == null || name.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name is required"));
        if (walkerRepo.existsByFlatNoAndWalkerName(flatNo, name.trim()))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Walker already exists in this flat"));
        StepWalker walker = walkerRepo.save(
                StepWalker.builder()
                        .flatNo(flatNo)
                        .walkerName(name.trim())
                        .build()
        );
        return ResponseEntity.ok(walker);
    }

    // ── Delete a walker ───────────────────────────────────
    @DeleteMapping("/walkers/{flatNo}/{walkerId}")
    public ResponseEntity<?> deleteWalker(
            @PathVariable String flatNo,
            @PathVariable Long walkerId) {
        return walkerRepo.findById(walkerId).map(w -> {
            if (!w.getFlatNo().equals(flatNo))
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Not authorised"));
            walkerRepo.delete(w);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Log / update steps for today ──────────────────────
    @PostMapping("/log/{flatNo}/{walkerId}")
    public ResponseEntity<?> logSteps(
            @PathVariable String flatNo,
            @PathVariable Long walkerId,
            @RequestBody Map<String, Integer> body) {
        Integer steps = body.get("steps");
        if (steps == null || steps < 0)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid step count"));

        return walkerRepo.findById(walkerId).map(walker -> {
            if (!walker.getFlatNo().equals(flatNo))
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Not authorised"));
            LocalDate date = today();
            StepLog log = logRepo.findByWalkerAndLogDate(walker, date)
                    .orElse(StepLog.builder()
                            .walker(walker)
                            .logDate(date)
                            .steps(0)
                            .build());
            log.setSteps(steps);
            return ResponseEntity.ok(logRepo.save(log));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Get today's steps for a walker ───────────────────
    @GetMapping("/today/{flatNo}/{walkerId}")
    public ResponseEntity<?> getTodaySteps(
            @PathVariable String flatNo,
            @PathVariable Long walkerId) {
        return walkerRepo.findById(walkerId).map(walker -> {
            if (!walker.getFlatNo().equals(flatNo))
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Not authorised"));
            int steps = logRepo.findByWalkerAndLogDate(walker, today())
                    .map(StepLog::getSteps).orElse(0);
            return ResponseEntity.ok(Map.of(
                    "steps", steps,
                    "date",  today().toString()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Get step history for a walker ────────────────────
    @GetMapping("/history/{flatNo}/{walkerId}")
    public ResponseEntity<?> getHistory(
            @PathVariable String flatNo,
            @PathVariable Long walkerId) {
        return walkerRepo.findById(walkerId).map(walker -> {
            if (!walker.getFlatNo().equals(flatNo))
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Not authorised"));
            return ResponseEntity.ok(
                    logRepo.findByWalkerOrderByLogDateDesc(walker)
            );
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Leaderboard ───────────────────────────────────────
    @GetMapping("/leaderboard")
    public ResponseEntity<?> leaderboard(
            @RequestParam(defaultValue = "today") String filter,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        LocalDate end   = today();
        LocalDate start = switch (filter) {
            case "today"  -> today();
            case "10days" -> today().minusDays(9);
            case "30days" -> today().minusDays(29);
            case "custom" -> (from != null) ? LocalDate.parse(from) : today();
            default       -> today();
        };
        if ("custom".equals(filter) && to != null) {
            end = LocalDate.parse(to);
        }

        List<Object[]> rows   = logRepo.leaderboard(start, end);
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rank",       rank++);
            entry.put("walkerId",   row[0]);
            entry.put("flatNo",     row[1]);
            entry.put("walkerName", row[2]);
            entry.put("totalSteps", row[3]);
            result.add(entry);
        }
        return ResponseEntity.ok(result);
    }
}