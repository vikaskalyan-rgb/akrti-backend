package com.akriti.apartment.controller;

import com.akriti.apartment.entity.CommunityPost;
import com.akriti.apartment.entity.CommunityPostComment;
import com.akriti.apartment.entity.CommunityPostLike;
import com.akriti.apartment.repository.CommunityPostCommentRepository;
import com.akriti.apartment.repository.CommunityPostLikeRepository;
import com.akriti.apartment.repository.CommunityPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/community-board")
public class CommunityBoardController {

    @Autowired private CommunityPostRepository        postRepo;
    @Autowired private CommunityPostLikeRepository    likeRepo;
    @Autowired private CommunityPostCommentRepository commentRepo;

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> enrich(CommunityPost p, String callerFlatNo) {
        int likes         = likeRepo.countByPost(p);
        int commentCount  = commentRepo.countByPost(p);
        boolean likedByMe = callerFlatNo != null && likeRepo.existsByPostAndFlatNo(p, callerFlatNo);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",           p.getId());
        m.put("category",     p.getCategory());
        m.put("title",        p.getTitle());
        m.put("content",      p.getContent());
        m.put("flatNo",       p.getFlatNo());
        m.put("userName",     p.getUserName());
        m.put("createdAt",    p.getCreatedAt());
        m.put("likes",        likes);
        m.put("commentCount", commentCount);
        m.put("likedByMe",    likedByMe);
        return m;
    }

    // ── GET all posts ─────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String flatNo) {
        List<CommunityPost> posts = postRepo.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(posts.stream().map(p -> enrich(p, flatNo)).toList());
    }

    // ── POST create post ──────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        CommunityPost post = CommunityPost.builder()
                .category(body.getOrDefault("category", "OTHER"))
                .title(body.get("title"))
                .content(body.get("content"))
                .flatNo(body.get("flatNo"))
                .userName(body.get("userName"))
                .build();
        CommunityPost saved = postRepo.save(post);
        return ResponseEntity.ok(enrich(saved, body.get("flatNo")));
    }

    // ── DELETE post ───────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return postRepo.findById(id).map(p -> {
            likeRepo.deleteByPostId(id);
            commentRepo.deleteByPostId(id);
            postRepo.delete(p);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── POST toggle like ──────────────────────────────────────────────────────
    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String flatNo   = body.get("flatNo");
        String userName = body.get("userName");

        return postRepo.findById(id).map(post -> {
            boolean liked;
            if (likeRepo.existsByPostAndFlatNo(post, flatNo)) {
                likeRepo.deleteByPostAndFlatNo(post, flatNo);
                liked = false;
            } else {
                likeRepo.save(CommunityPostLike.builder()
                        .post(post).flatNo(flatNo).userName(userName).build());
                liked = true;
            }
            return ResponseEntity.ok(Map.of("liked", liked, "likes", likeRepo.countByPost(post)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── GET comments ──────────────────────────────────────────────────────────
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        return postRepo.findById(id).map(post -> {
            List<CommunityPostComment> comments = commentRepo.findByPostOrderByCreatedAtAsc(post);
            return ResponseEntity.ok(comments.stream().map(c -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",        c.getId());
                m.put("flatNo",    c.getFlatNo());
                m.put("userName",  c.getUserName());
                m.put("content",   c.getContent());
                m.put("createdAt", c.getCreatedAt());
                return m;
            }).toList());
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── POST add comment ──────────────────────────────────────────────────────
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return postRepo.findById(id).map(post -> {
            CommunityPostComment comment = CommunityPostComment.builder()
                    .post(post)
                    .flatNo(body.get("flatNo"))
                    .userName(body.get("userName"))
                    .content(body.get("content"))
                    .build();
            CommunityPostComment saved = commentRepo.save(comment);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",        saved.getId());
            m.put("flatNo",    saved.getFlatNo());
            m.put("userName",  saved.getUserName());
            m.put("content",   saved.getContent());
            m.put("createdAt", saved.getCreatedAt());
            return ResponseEntity.ok(m);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE comment ────────────────────────────────────────────────────────
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        return commentRepo.findById(commentId).map(c -> {
            commentRepo.delete(c);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }
}