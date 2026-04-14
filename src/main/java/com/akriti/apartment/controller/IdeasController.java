package com.akriti.apartment.controller;

import com.akriti.apartment.entity.Idea;
import com.akriti.apartment.entity.IdeaComment;
import com.akriti.apartment.entity.IdeaUpvote;
import com.akriti.apartment.repository.IdeaCommentRepository;
import com.akriti.apartment.repository.IdeaRepository;
import com.akriti.apartment.repository.IdeaUpvoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ideas")
public class IdeasController {

    @Autowired private IdeaRepository        ideaRepo;
    @Autowired private IdeaUpvoteRepository  upvoteRepo;
    @Autowired private IdeaCommentRepository commentRepo;

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> enrich(Idea idea, String callerFlatNo) {
        int upvotes         = upvoteRepo.countByIdea(idea);
        int commentCount    = commentRepo.countByIdea(idea);
        boolean upvotedByMe = callerFlatNo != null && upvoteRepo.existsByIdeaAndFlatNo(idea, callerFlatNo);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",           idea.getId());
        m.put("category",     idea.getCategory());
        m.put("title",        idea.getTitle());
        m.put("description",  idea.getDescription());
        m.put("flatNo",       idea.getFlatNo());
        m.put("userName",     idea.getUserName());
        m.put("status",       idea.getStatus());
        m.put("createdAt",    idea.getCreatedAt());
        m.put("upvotes",      upvotes);
        m.put("commentCount", commentCount);
        m.put("upvotedByMe",  upvotedByMe);
        return m;
    }

    // ── GET all ideas ─────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String flatNo) {
        List<Idea> ideas = ideaRepo.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(ideas.stream().map(i -> enrich(i, flatNo)).toList());
    }

    // ── POST create idea ──────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        Idea idea = Idea.builder()
                .category(body.getOrDefault("category", "OTHER"))
                .title(body.get("title"))
                .description(body.get("description"))
                .flatNo(body.get("flatNo"))
                .userName(body.get("userName"))
                .build();
        Idea saved = ideaRepo.save(idea);
        return ResponseEntity.ok(enrich(saved, body.get("flatNo")));
    }

    // ── PATCH update status (admin only — enforced in frontend) ──────────────
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ideaRepo.findById(id).map(idea -> {
            idea.setStatus(body.get("status"));
            return ResponseEntity.ok(enrich(ideaRepo.save(idea), null));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE idea ───────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ideaRepo.findById(id).map(idea -> {
            upvoteRepo.deleteByIdeaId(id);
            commentRepo.deleteByIdeaId(id);
            ideaRepo.delete(idea);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── POST toggle upvote ────────────────────────────────────────────────────
    @PostMapping("/{id}/upvote")
    @Transactional
    public ResponseEntity<?> toggleUpvote(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String flatNo   = body.get("flatNo");
        String userName = body.get("userName");

        return ideaRepo.findById(id).map(idea -> {
            boolean upvoted;
            if (upvoteRepo.existsByIdeaAndFlatNo(idea, flatNo)) {
                upvoteRepo.deleteByIdeaAndFlatNo(idea, flatNo);
                upvoted = false;
            } else {
                upvoteRepo.save(IdeaUpvote.builder()
                        .idea(idea).flatNo(flatNo).userName(userName).build());
                upvoted = true;
            }
            return ResponseEntity.ok(Map.of(
                    "upvoted",  upvoted,
                    "upvotes",  upvoteRepo.countByIdea(idea)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── GET comments ──────────────────────────────────────────────────────────
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        return ideaRepo.findById(id).map(idea -> {
            List<IdeaComment> comments = commentRepo.findByIdeaOrderByCreatedAtAsc(idea);
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
        return ideaRepo.findById(id).map(idea -> {
            IdeaComment comment = IdeaComment.builder()
                    .idea(idea)
                    .flatNo(body.get("flatNo"))
                    .userName(body.get("userName"))
                    .content(body.get("content"))
                    .build();
            IdeaComment saved = commentRepo.save(comment);
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