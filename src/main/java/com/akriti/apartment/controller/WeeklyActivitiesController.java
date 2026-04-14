package com.akriti.apartment.controller;

import com.akriti.apartment.entity.ActivityRsvp;
import com.akriti.apartment.entity.WeeklyActivity;
import com.akriti.apartment.repository.ActivityRsvpRepository;
import com.akriti.apartment.repository.WeeklyActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/weekly-activities")
public class WeeklyActivitiesController {

    @Autowired private WeeklyActivityRepository activityRepo;
    @Autowired private ActivityRsvpRepository   rsvpRepo;

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> enrich(WeeklyActivity a, String callerFlatNo) {
        int attendeeCount   = rsvpRepo.countByActivity(a);
        boolean attendingMe = callerFlatNo != null && rsvpRepo.existsByActivityAndFlatNo(a, callerFlatNo);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",            a.getId());
        m.put("title",         a.getTitle());
        m.put("description",   a.getDescription());
        m.put("date",          a.getDate() != null ? a.getDate().toString() : null);
        m.put("time",          a.getTime() != null ? a.getTime().toString() : null);
        m.put("location",      a.getLocation());
        m.put("createdAt",     a.getCreatedAt());
        m.put("attendeeCount", attendeeCount);
        m.put("attendingMe",   attendingMe);
        return m;
    }

    // ── GET all activities ────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String flatNo) {
        List<WeeklyActivity> activities = activityRepo.findAllByOrderByDateAsc();
        return ResponseEntity.ok(activities.stream().map(a -> enrich(a, flatNo)).toList());
    }

    // ── POST create activity (admin only — enforced in frontend) ─────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        WeeklyActivity activity = WeeklyActivity.builder()
                .title(body.get("title"))
                .description(body.get("description"))
                .date(body.get("date") != null && !body.get("date").isBlank()
                        ? LocalDate.parse(body.get("date")) : null)
                .time(body.get("time") != null && !body.get("time").isBlank()
                        ? LocalTime.parse(body.get("time")) : null)
                .location(body.get("location"))
                .build();
        WeeklyActivity saved = activityRepo.save(activity);
        return ResponseEntity.ok(enrich(saved, null));
    }

    // ── DELETE activity ───────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return activityRepo.findById(id).map(a -> {
            rsvpRepo.deleteByActivityId(id);
            activityRepo.delete(a);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── GET RSVPs for an activity ─────────────────────────────────────────────
    @GetMapping("/{id}/rsvp")
    public ResponseEntity<?> getRsvps(@PathVariable Long id) {
        return activityRepo.findById(id).map(activity -> {
            List<ActivityRsvp> rsvps = rsvpRepo.findByActivity(activity);
            return ResponseEntity.ok(rsvps.stream().map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",        r.getId());
                m.put("flatNo",    r.getFlatNo());
                m.put("userName",  r.getUserName());
                m.put("createdAt", r.getCreatedAt());
                return m;
            }).toList());
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── POST toggle RSVP ──────────────────────────────────────────────────────
    @PostMapping("/{id}/rsvp")
    @Transactional
    public ResponseEntity<?> toggleRsvp(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String flatNo   = body.get("flatNo");
        String userName = body.get("userName");

        return activityRepo.findById(id).map(activity -> {
            boolean attending;
            if (rsvpRepo.existsByActivityAndFlatNo(activity, flatNo)) {
                rsvpRepo.deleteByActivityAndFlatNo(activity, flatNo);
                attending = false;
            } else {
                rsvpRepo.save(ActivityRsvp.builder()
                        .activity(activity).flatNo(flatNo).userName(userName).build());
                attending = true;
            }
            return ResponseEntity.ok(Map.of(
                    "attending", attending,
                    "count",     rsvpRepo.countByActivity(activity)));
        }).orElse(ResponseEntity.notFound().build());
    }
}