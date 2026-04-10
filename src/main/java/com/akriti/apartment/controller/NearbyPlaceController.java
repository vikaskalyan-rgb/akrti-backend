package com.akriti.apartment.controller;

import com.akriti.apartment.entity.NearbyPlace;
import com.akriti.apartment.entity.PlaceLike;
import com.akriti.apartment.entity.PlaceReview;
import com.akriti.apartment.repository.NearbyPlaceRepository;
import com.akriti.apartment.repository.PlaceLikeRepository;
import com.akriti.apartment.repository.PlaceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/places")
public class NearbyPlaceController {

    @Autowired private NearbyPlaceRepository  placeRepo;
    @Autowired private PlaceLikeRepository    likeRepo;
    @Autowired private PlaceReviewRepository  reviewRepo;

    // ── Helper: build enriched place response ─────────────
    private Map<String, Object> enrich(NearbyPlace p, String callerFlatNo) {
        int     likes    = likeRepo.countByPlace(p);
        int     reviews  = reviewRepo.countByPlace(p);
        boolean liked    = callerFlatNo != null && likeRepo.existsByPlaceAndFlatNo(p, callerFlatNo);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",        p.getId());
        m.put("name",      p.getName());
        m.put("category",  p.getCategory());
        m.put("address",   p.getAddress());
        m.put("phone",     p.getPhone());
        m.put("mapsUrl",   p.getMapsUrl());
        m.put("distance",  p.getDistance());
        m.put("addedBy",   p.getAddedBy());
        m.put("createdAt", p.getCreatedAt());
        m.put("likes",     likes);
        m.put("reviewCount", reviews);
        m.put("likedByMe", liked);
        return m;
    }

    // ── GET all places ────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String flatNo) {
        List<NearbyPlace> places = placeRepo.findByIsActiveTrueOrderByCategoryAscNameAsc();
        List<Map<String, Object>> result = places.stream()
                .map(p -> enrich(p, flatNo))
                .toList();
        return ResponseEntity.ok(result);
    }

    // ── POST add place (everyone can add) ─────────────────
    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, String> body) {
        NearbyPlace place = NearbyPlace.builder()
                .name(body.get("name"))
                .category(body.getOrDefault("category", "OTHER"))
                .address(body.get("address"))
                .phone(body.get("phone"))
                .mapsUrl(body.get("mapsUrl"))
                .distance(body.get("distance"))
                .addedBy(body.get("addedBy"))
                .build();
        return ResponseEntity.ok(placeRepo.save(place));
    }

    // ── PUT update place (admin only — enforce in frontend) ─
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return placeRepo.findById(id).map(p -> {
            if (body.containsKey("name"))     p.setName(body.get("name"));
            if (body.containsKey("category")) p.setCategory(body.get("category"));
            if (body.containsKey("address"))  p.setAddress(body.get("address"));
            if (body.containsKey("phone"))    p.setPhone(body.get("phone"));
            if (body.containsKey("mapsUrl"))  p.setMapsUrl(body.get("mapsUrl"));
            if (body.containsKey("distance")) p.setDistance(body.get("distance"));
            return ResponseEntity.ok(placeRepo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE place (admin only) ─────────────────────────
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return placeRepo.findById(id).map(p -> {
            reviewRepo.deleteByPlaceId(id);
            placeRepo.delete(p);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── POST toggle like ──────────────────────────────────
    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String flatNo   = body.get("flatNo");
        String userName = body.get("userName");

        return placeRepo.findById(id).map(place -> {
            boolean liked;
            if (likeRepo.existsByPlaceAndFlatNo(place, flatNo)) {
                likeRepo.deleteByPlaceAndFlatNo(place, flatNo);
                liked = false;
            } else {
                likeRepo.save(PlaceLike.builder()
                        .place(place)
                        .flatNo(flatNo)
                        .userName(userName)
                        .build());
                liked = true;
            }
            int count = likeRepo.countByPlace(place);
            return ResponseEntity.ok(Map.of("liked", liked, "likes", count));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── GET reviews for a place ───────────────────────────
    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long id) {
        return placeRepo.findById(id).map(place -> {
            List<PlaceReview> reviews = reviewRepo.findByPlaceOrderByCreatedAtDesc(place);
            return ResponseEntity.ok(reviews.stream().map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",         r.getId());
                m.put("flatNo",     r.getFlatNo());
                m.put("userName",   r.getUserName());
                m.put("reviewText", r.getReviewText());
                m.put("rating",     r.getRating());
                m.put("createdAt",  r.getCreatedAt());
                return m;
            }).toList());
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── POST add review ───────────────────────────────────
    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addReview(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return placeRepo.findById(id).map(place -> {
            PlaceReview review = PlaceReview.builder()
                    .place(place)
                    .flatNo((String) body.get("flatNo"))
                    .userName((String) body.get("userName"))
                    .reviewText((String) body.get("reviewText"))
                    .rating(body.get("rating") != null
                            ? ((Number) body.get("rating")).intValue() : 5)
                    .build();
            Map<String, Object> saved = new LinkedHashMap<>();
            PlaceReview s = reviewRepo.save(review);
            saved.put("id",         s.getId());
            saved.put("flatNo",     s.getFlatNo());
            saved.put("userName",   s.getUserName());
            saved.put("reviewText", s.getReviewText());
            saved.put("rating",     s.getRating());
            saved.put("createdAt",  s.getCreatedAt());
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE review (admin only) ────────────────────────
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        return reviewRepo.findById(reviewId).map(r -> {
            reviewRepo.delete(r);
            return ResponseEntity.ok(Map.of("message", "Review deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }
}